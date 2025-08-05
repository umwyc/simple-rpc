package com.wyc.simple.rpc.core.server.base;

import com.wyc.simple.rpc.core.SimpleRpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Rpc请求
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleRpcRequest implements Serializable {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 服务版本
     */
    private String serviceVersion = SimpleRpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 请求参数类型
     */
    private Class<?>[] parameterTypes;

    /**
     * 请求参数
     */
    private Object[] args;
}
