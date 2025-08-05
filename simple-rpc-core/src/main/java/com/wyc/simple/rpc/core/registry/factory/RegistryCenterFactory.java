package com.wyc.simple.rpc.core.registry.factory;

import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.registry.impl.EtcdRegistryCenter;
import com.wyc.simple.rpc.core.spi.SpiLoader;

/**
 * 注册中心工厂
 */
public class RegistryCenterFactory {

    static{
        SpiLoader.load(RegistryCenter.class);
    }

    /**
     * 默认的注册中心
     */
    public static RegistryCenter DEFAULT_REGISTRY_CENTER = new EtcdRegistryCenter();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static RegistryCenter getInstance(String key){
        return SpiLoader.getInstance(RegistryCenter.class, key);
    }
}
