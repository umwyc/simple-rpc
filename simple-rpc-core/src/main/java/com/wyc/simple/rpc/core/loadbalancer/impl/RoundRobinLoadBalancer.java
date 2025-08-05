package com.wyc.simple.rpc.core.loadbalancer.impl;

import com.wyc.simple.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RR 负载均衡器
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    /**
     * 当前轮询的下标
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList == null || serviceMetaInfoList.isEmpty()){
            return null;
        }
        if(serviceMetaInfoList.size() == 1){
            return serviceMetaInfoList.get(0);
        }

        // 重置轮询的下标
        if (currentIndex.get() == Integer.MAX_VALUE) {
            currentIndex.set(0);
        }
        return serviceMetaInfoList.get(currentIndex.getAndIncrement() % serviceMetaInfoList.size());
    }
}
