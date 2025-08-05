package com.wyc.simple.rpc.core.loadbalancer.impl;

import com.wyc.simple.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性哈希负载均衡器
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一致性 Hash 环，用于存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    /**
     * 虚拟节点数量
     */
    private final static int VIRTUAL_NODE_NUMS = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList == null || serviceMetaInfoList.isEmpty()){
            return null;
        }
        if(serviceMetaInfoList.size() == 1){
            return serviceMetaInfoList.get(0);
        }

        // 构建一致性 Hash 环
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUMS; i++) {
                String key = serviceMetaInfo.getServiceAddress() + "#" + i;
                int hash = getHashCode(key);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }

        // 根据 Hash 码从环上获取节点
        int hash = getHashCode(requestParams);
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
        if(entry == null){
            entry = virtualNodes.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * 对象哈希值
     * @param key
     * @return
     */
    private int getHashCode(Object key){
        return key.hashCode();
    }
}
