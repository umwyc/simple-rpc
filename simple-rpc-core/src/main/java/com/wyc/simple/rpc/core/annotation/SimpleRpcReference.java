package com.wyc.simple.rpc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleRpcReference {

    /**
     * 要调用的接口类
     * @return
     */
    Class<?> serviceClass() default void.class;


}
