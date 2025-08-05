package com.wyc.simple.rpc.core.toolkit;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存服务提供类
 */
public class LocalServiceCache {

    /**
     * key (接口全路径名) ===> value (服务实现类)
     */
    private static final ConcurrentHashMap<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName
     * @param implClass
     */
    public static void register(String serviceName, Class<?> implClass){
        map.put(serviceName, implClass);
    }

    /**
     * 获取服务
     * @param serviceName
     */
    public static Class<?> get(String serviceName){
        return map.get(serviceName);
    }

    /**
     * 删除服务
     * @param serviceName
     */
    public static void remove(String serviceName){
        map.remove(serviceName);
    }
}
