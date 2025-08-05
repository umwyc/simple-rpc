package com.wyc.simple.rpc.core.registry.constant;

/**
 * 支持多种注册中心
 */
public interface RegistryCenterKeys {

    /**
     * etcd 注册中心
     */
    String ETCD = "etcd";

    /**
     * zookeeper 注册中心
     */
    String ZOOKEEPER = "zookeeper";

    /**
     * nacos 注册中心
     */
    String NACOS = "nacos";
}
