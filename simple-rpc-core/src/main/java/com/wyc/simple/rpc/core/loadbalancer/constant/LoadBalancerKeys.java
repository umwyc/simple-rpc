package com.wyc.simple.rpc.core.loadbalancer.constant;

/**
 * 支持多种负载均衡机制
 */
public interface LoadBalancerKeys {

    String ROUND_ROBIN = "roundRobin";

    String RANDOM = "random";

    String CONSISTENT_HASH = "consistentHash";
}
