package com.wyc.simple.rpc.core.annotation;

import com.wyc.simple.rpc.core.annotation.autoconfig.SimpleRpcConsumerAutoConfig;
import com.wyc.simple.rpc.core.annotation.autoconfig.SimpleRpcInitAutoConfig;
import com.wyc.simple.rpc.core.annotation.autoconfig.SimpleRpcProviderAutoConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SimpleRpcInitAutoConfig.class, SimpleRpcProviderAutoConfig.class, SimpleRpcConsumerAutoConfig.class})
public @interface EnableSimpleRpc {

    String protocol() default "simple-rpc";

}
