package com.wyc.simple.rpc.core.annotation.autoconfig;

import cn.hutool.core.util.StrUtil;
import com.wyc.simple.rpc.core.SimpleRpcApplication;
import com.wyc.simple.rpc.core.annotation.SimpleRpcReference;
import com.wyc.simple.rpc.core.client.ServiceProxyFactory;
import com.wyc.simple.rpc.core.config.SimpleRpcConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * simple-rpc 服务消费者自动配置类
 * 需要自动通过生成代理类通过 HTTP 协议或者 SimpleRpc 协议来调用 RPC 服务
 */
@Slf4j
public class SimpleRpcConsumerAutoConfig implements BeanPostProcessor {


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            SimpleRpcReference simpleRpcReference = field.getAnnotation(SimpleRpcReference.class);
            if (simpleRpcReference != null) {
                Class<?> serviceClass = simpleRpcReference.serviceClass();
                if (serviceClass == null || serviceClass == void.class) {
                    throw new RuntimeException("[SimpleRpcConsumerAutoConfig interfaceClass非法]");
                }

                try {
                    // 获取协议类型
                    SimpleRpcConfig simpleRpcConfig = SimpleRpcApplication.getSimpleRpcConfig();
                    String protocol = simpleRpcConfig.getProtocol();
                    if (StrUtil.isBlank(protocol)) {
                        protocol = "http";
                    }
                    boolean isHttp = "http".equals(protocol);

                    // 创建代理对象
                    Object data = ServiceProxyFactory.getProxy(serviceClass, isHttp);

                    // 设置字段可访问并赋值
                    field.setAccessible(true);
                    field.set(bean, data);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    log.error("[SimpleRpcConsumerAutoConfig 设置字段值失败 field={}, bean={}]", field.getName(), beanName, e);
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    log.error("[SimpleRpcConsumerAutoConfig 创建代理对象失败 interfaceClass={}]", serviceClass.getName(), e);
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
