package com.wyc.simple.rpc.core.registry;

import java.util.List;

import com.wyc.simple.rpc.core.config.RegistryCenterConfig;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;

public interface RegistryCenter {

    /**
     * 初始化注册中心
     * @param registryCenterConfig
     */
    void init(RegistryCenterConfig registryCenterConfig);

    /**
     * 注册服务
     * @param serviceMetaInfo
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 服务发现
     * @param serviceKey
     * @return
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     * 心跳检测（提供者）
     */
    void heartBeat();

    /**
     * 监听
     * @param serviceNodeKey
     */
    void watch(String serviceNodeKey);

    /**
     * 注销服务
     * @param serviceMetaInfo
     */
    void unregister(ServiceMetaInfo serviceMetaInfo);


    /**
     *
     * 服务销毁
     */
    void destroy();
}
