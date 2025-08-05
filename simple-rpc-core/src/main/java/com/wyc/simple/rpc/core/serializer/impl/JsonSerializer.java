package com.wyc.simple.rpc.core.serializer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyc.simple.rpc.core.server.base.SimpleRpcRequest;
import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;
import com.wyc.simple.rpc.core.serializer.Serializer;

import java.io.IOException;

/**
 * Json 序列化器
 */
public class JsonSerializer implements Serializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> classType) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes, classType);
        if (obj instanceof SimpleRpcRequest) {
            return handleRequest((SimpleRpcRequest) obj, classType);
        }
        if (obj instanceof SimpleRpcResponse) {
            return handleResponse((SimpleRpcResponse) obj, classType);
        }
        return obj;
    }

    /**
     * 由于 Object 的原始对象会被擦除，导致反序列化时会被作为 LinkedHashMap 无法转换成原始对象，因此这里做了特殊处理
     * @param simpleRpcRequest simple-rpc 请求
     * @param type       类型
     * @return {@link T}
     * @throws IOException IO异常
     */
    private <T> T handleRequest(SimpleRpcRequest simpleRpcRequest, Class<T> type) throws IOException {
        Class<?>[] parameterTypes = simpleRpcRequest.getParameterTypes();
        Object[] args = simpleRpcRequest.getArgs();

        // 循环处理每个参数的类型
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            // 如果类型不同，则重新处理一下类型
            if (!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(argBytes, clazz);
            }
        }
        return type.cast(simpleRpcRequest);
    }

    /**
     * 由于 Object 的原始对象会被擦除，导致反序列化时会被作为 LinkedHashMap 无法转换成原始对象，因此这里做了特殊处理
     *
     * @param simpleRpcResponse simple-rpc 响应
     * @param type        类型
     * @return {@link T}
     * @throws IOException IO异常
     */
    private <T> T handleResponse(SimpleRpcResponse simpleRpcResponse, Class<T> type) throws IOException {
        // 处理响应数据
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(simpleRpcResponse.getData());
        simpleRpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes, simpleRpcResponse.getDataType()));
        return type.cast(simpleRpcResponse);
    }
}
