package com.wyc.simple.rpc.core.loadbalancer;

import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 客户端负载均衡器
 */
public interface LoadBalancer {

    ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);

}
