package com.wyc.simple.rpc.core.annotation;

import com.wyc.simple.rpc.core.annotation.autoconfig.SimpleRpcConsumerAutoConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端启动注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SimpleRpcConsumerAutoConfig.class})
public @interface EnableSimpleRpcClient {
}
