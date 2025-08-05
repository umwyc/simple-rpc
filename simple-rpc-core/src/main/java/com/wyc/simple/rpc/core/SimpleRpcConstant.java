package com.wyc.simple.rpc.core;

/**
 * rpc相关常量
 */
public interface SimpleRpcConstant {

    /**
     * 配置文件 application.yaml 加载前缀
     */
    public static String DEFAULT_CONFIG_PREFIX = "simple-rpc";

    /**
     * 默认服务版本
     */
    public static String DEFAULT_SERVICE_VERSION = "1.0.0";

    /**
     * 默认负载均衡器
     */
    public static String DEFAULT_LOAD_BALANCER = "roundRobin";

    /**
     * 默认重试机制
     */
    public static String DEFAULT_RETRY_STRATEGY = "no";
}
