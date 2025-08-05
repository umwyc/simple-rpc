package com.wyc.simple.rpc.core.loadbalancer.impl;

import com.wyc.simple.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机负载均衡器
 */
public class RandomLoadBalancer implements LoadBalancer {

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList == null || serviceMetaInfoList.isEmpty()){
            return null;
        }
        if(serviceMetaInfoList.size() == 1){
            return serviceMetaInfoList.get(0);
        }

        return serviceMetaInfoList.get(new Random().nextInt(serviceMetaInfoList.size()));
    }
}
