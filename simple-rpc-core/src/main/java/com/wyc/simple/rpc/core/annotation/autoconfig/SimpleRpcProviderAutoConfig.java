package com.wyc.simple.rpc.core.annotation.autoconfig;

import com.wyc.simple.rpc.core.SimpleRpcApplication;
import com.wyc.simple.rpc.core.annotation.SimpleRpcService;
import com.wyc.simple.rpc.core.config.RegistryCenterConfig;
import com.wyc.simple.rpc.core.config.SimpleRpcConfig;
import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;
import com.wyc.simple.rpc.core.registry.factory.RegistryCenterFactory;
import com.wyc.simple.rpc.core.toolkit.LocalServiceCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * simple-rpc 服务提供者自动配置类
 * 将 Provider 提供的服务自动注册到本地以及注册中心
 */
@Slf4j
public class SimpleRpcProviderAutoConfig implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        SimpleRpcService simpleRpcService = bean.getClass().getAnnotation(SimpleRpcService.class);
        if (simpleRpcService != null) {
            Class<?> interfaceClass = simpleRpcService.interfaceClass();
            if (interfaceClass == null || interfaceClass == void.class) {
                throw new RuntimeException("[SimpleRpcProviderAutoConfig interfaceClass非法]");
            }
            // 将 bean 注册到本地缓存
            LocalServiceCache.register(interfaceClass.getName(), bean.getClass(), bean);
            // 将服务实例注册到注册中心
            SimpleRpcConfig simpleRpcConfig = SimpleRpcApplication.getSimpleRpcConfig();
            RegistryCenterConfig registryCenterConfig = simpleRpcConfig.getRegistryCenterConfig();
            RegistryCenter registryCenter = RegistryCenterFactory.getInstance(registryCenterConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = ServiceMetaInfo.builder()
                    .serviceName(interfaceClass.getName())
                    .serviceHost(simpleRpcConfig.getServerHost())
                    .servicePort(simpleRpcConfig.getServerPort())
                    .serviceVersion(simpleRpcConfig.getVersion())
                    .build();
            try {
                registryCenter.register(serviceMetaInfo);
            } catch (Exception e) {
                log.error("[SimpleRpcProviderAutoConfig 注册服务失败]", e);
                throw new RuntimeException(e);
            }

        }
        return bean;
    }
}
