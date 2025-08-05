package com.wyc.simple.rpc.core.annotation;

import com.wyc.simple.rpc.core.SimpleRpcConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleRpcReference {

    /**
     * 接口
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务版本号
     * @return
     */
    String serviceVersion() default SimpleRpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 负载均衡器
     * @return
     */
    String loadBalancer() default SimpleRpcConstant.DEFAULT_LOAD_BALANCER;

    /**
     * 重试机制
     * @return
     */
    String retryStrategy() default SimpleRpcConstant.DEFAULT_RETRY_STRATEGY;

}
