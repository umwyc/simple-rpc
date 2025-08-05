package com.wyc.simple.rpc.core;

import com.alibaba.fastjson.JSON;
import com.wyc.simple.rpc.core.config.RegistryCenterConfig;
import com.wyc.simple.rpc.core.config.SimpleRpcConfig;
import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.registry.factory.RegistryCenterFactory;
import com.wyc.simple.rpc.core.toolkit.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Rpc框架全局配置加载器
 */
@Slf4j
public class SimpleRpcApplication {

    private static volatile SimpleRpcConfig simpleRpcConfig;

    /**
     * 初始化框架
     */
    public static void init(SimpleRpcConfig newSimpleRpcConfig){
        simpleRpcConfig = newSimpleRpcConfig;
        // 更新配置的时候记录记录日志
        log.info("[simple-rpc init, config = {}]", JSON.toJSONString(simpleRpcConfig));
        // 注册中心初始化
        RegistryCenterConfig registryCenterConfig = simpleRpcConfig.getRegistryCenterConfig();
        RegistryCenter registryCenter = RegistryCenterFactory.getInstance(registryCenterConfig.getRegistry());
        registryCenter.init(registryCenterConfig);
        log.info("[registryCenter init, config = {}]", JSON.toJSONString(registryCenterConfig));
        // 退出虚拟机之前执行销毁注册中心的 hook
        Runtime.getRuntime().addShutdownHook(new Thread(registryCenter::destroy));
    }

    /**
     * 初始化框架
     */
    public static void init(){
        SimpleRpcConfig newSimpleRpcConfig;
        try {
            // 从 application.yaml 文件中读取 simple-rpc 配置
            newSimpleRpcConfig = ConfigUtil.loadConfig(SimpleRpcConstant.DEFAULT_CONFIG_PREFIX, SimpleRpcConfig.class);
        } catch (Exception e) {
            // 读取失败，走兜底逻辑
            log.error("[simple-rpc init failed, error = {}]", JSON.toJSONString(e));
            newSimpleRpcConfig = new SimpleRpcConfig();
        }
        init(newSimpleRpcConfig);
    }

    /**
     * 获取 simple-rpc 配置
     * @return
     */
    public static SimpleRpcConfig getSimpleRpcConfig(){
        if(simpleRpcConfig == null){
            synchronized (SimpleRpcApplication.class){
                if(simpleRpcConfig == null){
                    init();
                }
            }
        }
        return simpleRpcConfig;
    }
}
