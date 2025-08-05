package com.wyc.simple.rpc.core.registry.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import com.wyc.simple.rpc.core.config.RegistryCenterConfig;
import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * zookeeper 注册中心
 */
@Slf4j
public class ZooKeeperRegistryCenter implements RegistryCenter {

    private CuratorFramework client;

    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;

    /**
     * 本机注册的节点 key 集合
     */
    private final Set<String> NODE_KEY_CACHE = new HashSet<>();

    /**
     * 注册中心拉取的本地服务缓存 (key 类的全路径名 ===> value 拉取到的服务实例列表)
     */
    private final Map<String, List<ServiceMetaInfo>> SERVICE_META_INFO_CACHE = new ConcurrentHashMap<>();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> WATCHING_KEY_SET = new ConcurrentHashSet<>();

    /**
     * 根节点
     */
    private static final String ZK_ROOT_PATH = "/simple-rpc";


    @Override
    public void init(RegistryCenterConfig registryCenterConfig) {
        // 构建 client
        client = CuratorFrameworkFactory
                .builder()
                .connectString(registryCenterConfig.getAddress())
                .retryPolicy(new ExponentialBackoffRetry(Math.toIntExact(registryCenterConfig.getTimeout()), 3))
                .build();

        // 构建 serviceDiscovery 实例
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();

        try {
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 注册到注册中心
        serviceDiscovery.registerService(buildServiceInstance(serviceMetaInfo));

        // 本地缓存节点 key
        String nodeKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        NODE_KEY_CACHE.add(nodeKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 命中缓存
        if (SERVICE_META_INFO_CACHE.containsKey(serviceKey) && CollUtil.isNotEmpty(SERVICE_META_INFO_CACHE.get(serviceKey))) {
            return SERVICE_META_INFO_CACHE.get(serviceKey);
        }

        try {
            // 拉取注册中心的服务
            Collection<ServiceInstance<ServiceMetaInfo>> serviceInstanceList = serviceDiscovery.queryForInstances(serviceKey);
            List<ServiceMetaInfo> serviceMetaInfoList = serviceInstanceList.stream()
                    .map(serviceInstance -> {
                        ServiceMetaInfo serviceMetaInfo = serviceInstance.getPayload();
                        watch(serviceMetaInfo.getServiceNodeKey());
                        return serviceMetaInfo;
                    })
                    .collect(Collectors.toList());

            // 添加到本地缓存
            SERVICE_META_INFO_CACHE.put(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("[ZooKeeperRegistryCenter 从注册中心拉取服务失败]", e);
        }
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        try {
            serviceDiscovery.unregisterService(buildServiceInstance(serviceMetaInfo));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 从本地缓存移除
        String nodeKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        NODE_KEY_CACHE.remove(nodeKey);
    }

    @Override
    public void heartBeat() {
        // Zookeeper 自带心跳机制
    }

    /**
     * 监听
     * @param serviceNodeKey 服务节点 key
     */
    @Override
    public void watch(String serviceNodeKey) {
        String watchKey = ZK_ROOT_PATH + "/" + serviceNodeKey;
        boolean res = WATCHING_KEY_SET.add(watchKey);
        if (res) {
            CuratorCache curatorCache = CuratorCache.build(client, watchKey);
            curatorCache.start();
            curatorCache.listenable().addListener(
                    CuratorCacheListener
                            .builder()
                            .forDeletes(node -> {
                                try {
                                    // 路径解析
                                    String path = node.getPath();
                                    String[] parts = path.split("/");
                                    if (parts.length < 3) {
                                        log.warn("[ZooKeeperRegistryCenter 无效删除路径: {}", path);
                                        return;
                                    }
                                    String serviceKey = parts[2];
                                    String nodeKey = path.substring((ZK_ROOT_PATH + "/").length());

                                    // 精准删除单个实例
                                    List<ServiceMetaInfo> serviceList = SERVICE_META_INFO_CACHE.get(serviceKey);
                                    if (serviceList != null) {
                                        boolean removed = serviceList.removeIf(metaInfo ->
                                                nodeKey.equals(metaInfo.getServiceNodeKey())
                                        );
                                        if (removed) {
                                            log.info("[ZooKeeperRegistryCenter 服务实例 {} 已从缓存移除]", nodeKey);
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("[ZooKeeperRegistryCenter 处理删除事件失败]", e);
                                }
                            })
                            .forChanges(((oldNode, newNode) -> {
                                try {
                                    // 路径解析
                                    String path = newNode.getPath();
                                    String[] parts = path.split("/");
                                    if (parts.length < 3) {
                                        log.warn("[ZooKeeperRegistryCenter 无效更新路径: {}", path);
                                        return;
                                    }
                                    String serviceKey = parts[2];
                                    String nodeKey = path.substring((ZK_ROOT_PATH + "/").length());

                                    // 获取更新后的实例数据
                                    byte[] data = newNode.getData();
                                    if (data == null || data.length == 0) {
                                        log.warn("[ZooKeeperRegistryCenter 节点 {} 数据为空]", path);
                                        return;
                                    }
                                    JsonInstanceSerializer<ServiceMetaInfo> serializer = new JsonInstanceSerializer<>(ServiceMetaInfo.class);
                                    ServiceInstance<ServiceMetaInfo> newInstance = serializer.deserialize(data);
                                    ServiceMetaInfo newMetaInfo = newInstance.getPayload();

                                    // 精准更新单个实例
                                    List<ServiceMetaInfo> serviceList = SERVICE_META_INFO_CACHE.get(serviceKey);
                                    if (serviceList != null) {
                                        for (int i = 0; i < serviceList.size(); i++) {
                                            if (nodeKey.equals(serviceList.get(i).getServiceNodeKey())) {
                                                serviceList.set(i, newMetaInfo);
                                                log.info("[ZooKeeperRegistryCenter 服务实例 {} 已更新]", nodeKey);
                                                break;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("[ZooKeeperRegistryCenter 处理更新事件失败]", e);
                                }
                            }))
                            .build()
            );
        }
    }

    @Override
    public void destroy() {
        for (String key : NODE_KEY_CACHE) {
            try {
                client.delete().guaranteed().forPath(key);
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }

        if (client != null) {
            client.close();
        }
    }

    private ServiceInstance<ServiceMetaInfo> buildServiceInstance(ServiceMetaInfo serviceMetaInfo) {
        String serviceAddress = serviceMetaInfo.getServiceHost() + ":" + serviceMetaInfo.getServicePort();
        try {
            return ServiceInstance
                    .<ServiceMetaInfo>builder()
                    .id(serviceAddress)
                    .name(serviceMetaInfo.getServiceKey())
                    .address(serviceAddress)
                    .payload(serviceMetaInfo)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
