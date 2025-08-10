package com.wyc.simple.rpc.core.annotation;

import com.wyc.simple.rpc.core.SimpleRpcConstant;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface SimpleRpcService {

    /**
     * 接口类
     * @return
     */
    Class<?> interfaceClass() default void.class;

}
