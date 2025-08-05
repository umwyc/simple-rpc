package com.wyc.simple.rpc.core.loadbalancer.factory;

import com.wyc.simple.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.simple.rpc.core.loadbalancer.impl.RoundRobinLoadBalancer;
import com.wyc.simple.rpc.core.spi.SpiLoader;

/**
 * 负载均衡器工厂
 */
public class LoadBalancerFactory {

    static{
        SpiLoader.load(LoadBalancer.class);
    }

    /**
     * 默认负载均衡器为轮询负载均衡器
     */
    public static final LoadBalancer DEFAULT_LOADBALANCER = new RoundRobinLoadBalancer();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static LoadBalancer getInstance(String key){
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
