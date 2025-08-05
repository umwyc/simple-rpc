package com.wyc.simple.rpc.core.annotation.autoconfig;

import com.alibaba.fastjson.JSON;
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
            Class<?> serviceClass = simpleRpcService.serviceClass();
            String serviceName = serviceClass.getName();
            String serviceVersion = simpleRpcService.serviceVersion();

            // 注册本地服务
            LocalServiceCache.register(serviceName, serviceClass);

            // 推送服务至注册中心
            SimpleRpcConfig simpleRpcConfig = SimpleRpcApplication.getSimpleRpcConfig();
            RegistryCenterConfig registryCenterConfig = simpleRpcConfig.getRegistryCenterConfig();
            RegistryCenter registryCenter = RegistryCenterFactory.getInstance(registryCenterConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(simpleRpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(simpleRpcConfig.getServerPort());

            try {
                registryCenter.register(serviceMetaInfo);
            } catch (Exception e) {
                log.error("[SimpleRpcProviderAutoConfig 推送服务至注册中心失败 details:{}]", JSON.toJSONString(e));
                throw new RuntimeException(e);
            }
        }

        return bean;
    }
}
