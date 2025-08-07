package com.wyc.simple.rpc.core.toolkit;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存服务提供类
 */
public class LocalServiceCache {

    /**
     * key (接口全路径名) ===> value (服务实现类)
     */
    private static final ConcurrentHashMap<String, Class<?>> CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * key (服务实现类全路径名） ===> 单例服务对象
     */
    private static final ConcurrentHashMap<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName 接口全路径名
     * @param implClass 服务实现类
     * @param bean 服务单例对象
     */
    public static void register(String serviceName, Class<?> implClass, Object bean){
        CLASS_MAP.put(serviceName, implClass);
        OBJECT_MAP.put(implClass.getName(), bean);
    }

    /**
     * 获取服务类
     * @param serviceName 接口全路径名
     */
    public static Class<?> getClass(String serviceName){
        return CLASS_MAP.get(serviceName);
    }

    /**
     * 获取服务单例对象
     * @param serviceName 接口全路径名
     */
    public static Object getBean(String serviceName) {
        if (!CLASS_MAP.containsKey(serviceName)) {
            return null;
        }
        Class<?> serviceClass = CLASS_MAP.get(serviceName);
        if (!OBJECT_MAP.containsKey(serviceClass.getName())) {
            return null;
        }
        return OBJECT_MAP.get(serviceClass.getName());
    }

    /**
     * 删除服务
     * @param serviceName 接口全路径名
     */
    public static void remove(String serviceName){
        Class<?> tClass = CLASS_MAP.get(serviceName);
        if (tClass == null) {
            return;
        }
        CLASS_MAP.remove(serviceName);
        OBJECT_MAP.remove(tClass.getName());
    }
}
