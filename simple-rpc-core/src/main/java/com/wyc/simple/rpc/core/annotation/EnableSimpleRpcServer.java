package com.wyc.simple.rpc.core.annotation;

import com.wyc.simple.rpc.core.annotation.autoconfig.SimpleRpcProviderAutoConfig;
import com.wyc.simple.rpc.core.annotation.autoconfig.SimpleRpcServerBootstrapAutoConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务启动类注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SimpleRpcServerBootstrapAutoConfig.class, SimpleRpcProviderAutoConfig.class})
public @interface EnableSimpleRpcServer {

}
