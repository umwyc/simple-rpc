package com.wyc.simple.rpc.core.config;

import com.wyc.simple.rpc.core.loadbalancer.constant.LoadBalancerKeys;
import com.wyc.simple.rpc.core.retry.constant.RetryStrategyKeys;
import com.wyc.simple.rpc.core.serializer.constant.SerializerKeys;
import lombok.Data;

/**
 * Rpc框架全局配置类
 */
@Data
public class SimpleRpcConfig {

    /**
     * 名称
     */
    private String name = "simple-rpc";

    /**
     * 版本号
     */
    private String version = "1.0.0";

    /**
     * 服务器的主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器使用的端口号
     */
    private Integer serverPort = 8080;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 注册中心配置
     */
    RegistryCenterConfig registryCenterConfig = new RegistryCenterConfig();

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 失败重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;
}
