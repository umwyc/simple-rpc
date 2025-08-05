package com.wyc.simple.rpc.core.registry.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.alibaba.fastjson2.JSON;
import com.wyc.simple.rpc.core.config.RegistryCenterConfig;
import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class EtcdRegistryCenter implements RegistryCenter {

    private static Client client;

    private static KV kvClient;

    /**
     * 根节点
     */
    private final String ETCD_ROOT_PATH = "/simple-rpc";

    /**
     * 本机注册的节点 key 集合
     */
    private final Set<String> NODE_KEY_CACHE = new HashSet<>();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> WATCHING_KEY_SET = new HashSet<>();

    /**
     * 注册中心拉取的本地服务缓存 (key 类的全路径名 ===> value 拉取到的服务实例列表)
     */
    private final Map<String, List<ServiceMetaInfo>> SERVICE_META_INFO_CACHE = new ConcurrentHashMap<>();

    @Override
    public void init(RegistryCenterConfig registryCenterConfig) {
        client = Client.builder()
                .endpoints(registryCenterConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryCenterConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        // 启动心跳检测 每隔10s向注册中心续约
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建租约客户端
        Lease leaseClient = client.getLeaseClient();
        long leaseId = leaseClient.grant(30L).get().getID();

        // 将节点信息注册到注册中心
        String nodeKey = ETCD_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(nodeKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSON.toJSONString(serviceMetaInfo), StandardCharsets.UTF_8);
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // 本地缓存节点 key
        NODE_KEY_CACHE.add(nodeKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 命中缓存
        if (SERVICE_META_INFO_CACHE.containsKey(serviceKey) && CollUtil.isNotEmpty(SERVICE_META_INFO_CACHE.get(serviceKey))) {
            return SERVICE_META_INFO_CACHE.get(serviceKey);
        }

        // 远程拉取注册中心的服务
        String servicePrefix = ETCD_ROOT_PATH + "/" + serviceKey + "/";
        GetOption getOption = GetOption.builder().isPrefix(true).build();
        try {
            List<KeyValue> kvs = kvClient.get(
                            ByteSequence.from(servicePrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();

            List<ServiceMetaInfo> serviceMetaInfoList = kvs.stream().map(kv -> {
                String nodeKey = kv.getKey().toString(StandardCharsets.UTF_8);
                // 监听 nodeKey
                watch(nodeKey);
                String value = kv.getValue().toString(StandardCharsets.UTF_8);
                return JSON.parseObject(value, ServiceMetaInfo.class);
            }).collect(Collectors.toList());

            // 添加到本地缓存
            SERVICE_META_INFO_CACHE.put(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("[EtcdRegistryCenter 拉取注册中心服务失败]", e);
        }
    }

    @Override
    public void heartBeat() {
        CronUtil.schedule("*/10 * * * * *", (Task) () -> {
            // 遍历本地注册的所有节点
            for (String nodeKey : NODE_KEY_CACHE) {
                try {
                    List<KeyValue>  kvs = kvClient.get(ByteSequence.from(nodeKey, StandardCharsets.UTF_8))
                            .get()
                            .getKvs();

                    // 注册中心节点过期 移除本地缓存
                    if (CollUtil.isEmpty(kvs)) {
                        NODE_KEY_CACHE.remove(nodeKey);
                        continue;
                    }

                    for (KeyValue kv : kvs) {
                        String value = kv.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSON.parseObject(value, ServiceMetaInfo.class);
                        // 节点续约
                        register(serviceMetaInfo);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("[EtcdRegistryCenter " + nodeKey + " 节点续约失败]", e);
                }
            }
        });

        // 支持秒级定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听，开启监听
        boolean add = WATCHING_KEY_SET.add(serviceNodeKey);
        if (add) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), watchResponse -> {
                for (WatchEvent event : watchResponse.getEvents()) {
                    String value;
                    ServiceMetaInfo serviceMetaInfo;
                    switch (event.getEventType()) {
                        case DELETE:
                            // 注册中心删除节点后清除本地缓存
                            value = event.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                            serviceMetaInfo = JSON.parseObject(value, ServiceMetaInfo.class);
                            WATCHING_KEY_SET.remove(serviceMetaInfo.getServiceNodeKey());
                            SERVICE_META_INFO_CACHE.get(serviceMetaInfo.getServiceKey()).remove(serviceMetaInfo);
                            log.info("[EtcdRegistryCenter 注册中心删除节点本地缓存变更 WATCHING_KEY_SET={}, SERVICE_META_INFO_CACHE={}]"
                                    , JSON.toJSONString(WATCHING_KEY_SET), JSON.toJSONString(SERVICE_META_INFO_CACHE));
                            break;

                        case PUT:
                            // 注册中心新增一个节点或者更新一个节点
                            value = event.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                            serviceMetaInfo = JSON.parseObject(value, ServiceMetaInfo.class);
                            WATCHING_KEY_SET.add(serviceMetaInfo.getServiceNodeKey());

                            List<ServiceMetaInfo> serviceMetaInfoList = SERVICE_META_INFO_CACHE.computeIfAbsent(serviceMetaInfo.getServiceKey(), serviceKey -> new ArrayList<>());
                            synchronized (serviceMetaInfoList) {
                                serviceMetaInfoList.removeIf(each ->
                                        each.getServiceNodeKey().equals(serviceMetaInfo.getServiceNodeKey())
                                );
                                serviceMetaInfoList.add(serviceMetaInfo);
                            }

                            log.info("[EtcdRegistryCenter 注册中心新增或更新节点本地缓存变更 WATCHING_KEY_SET={}, SERVICE_META_INFO_CACHE={}]"
                                    , JSON.toJSONString(WATCHING_KEY_SET), JSON.toJSONString(SERVICE_META_INFO_CACHE));

                        default:
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        // 从注册中心删除节点
        String nodeKey = ETCD_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(nodeKey, StandardCharsets.UTF_8));

        // 删除本地缓存
        NODE_KEY_CACHE.remove(nodeKey);
    }

    @Override
    public void destroy() {
        for (String registerKey : NODE_KEY_CACHE) {
            try {
                // 从注册中心删除节点
                kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
