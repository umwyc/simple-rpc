package com.wyc.simple.rpc.core;

import com.alibaba.fastjson.JSON;
import com.wyc.simple.rpc.core.config.RegistryCenterConfig;
import com.wyc.simple.rpc.core.config.SimpleRpcConfig;
import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.registry.factory.RegistryCenterFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Rpc框架全局配置加载器
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({SimpleRpcConfig.class, RegistryCenterConfig.class})
public class SimpleRpcApplication {

    @Getter
    private static volatile SimpleRpcConfig simpleRpcConfig;

    @Autowired
    public void setSimpleRpcConfig(SimpleRpcConfig simpleRpcConfig){
        SimpleRpcApplication.simpleRpcConfig = simpleRpcConfig;
    }

    @PostConstruct
    public void init() {
        initRegistryCenter();
    }
    /**
     * 初始化注册中心
     */
    private void initRegistryCenter() {
        try {
            RegistryCenterConfig registryCenterConfig = simpleRpcConfig.getRegistryCenterConfig();
            RegistryCenter registryCenter = RegistryCenterFactory.getInstance(registryCenterConfig.getRegistry());
            registryCenter.init(registryCenterConfig);
            log.info("[SimpleRpcApplication 初始化注册中心成功 registryCenterConfig = {}]", JSON.toJSONString(registryCenterConfig));
            // 退出虚拟机之前执行节点下线的 hook
            Runtime.getRuntime().addShutdownHook(new Thread(registryCenter::destroy));
        } catch (Exception exception) {
            log.error("[SimpleRpcApplication 初始化注册中心失败]", exception);
        }
    }

}
