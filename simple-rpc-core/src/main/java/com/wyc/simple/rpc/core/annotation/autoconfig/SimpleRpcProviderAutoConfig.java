package com.wyc.simple.rpc.core.annotation.autoconfig;

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


        return bean;
    }
}
