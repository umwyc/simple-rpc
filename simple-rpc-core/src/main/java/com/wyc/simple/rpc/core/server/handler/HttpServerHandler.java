package com.wyc.simple.rpc.core.server.handler;

import com.alibaba.fastjson.JSON;
import com.wyc.simple.rpc.core.SimpleRpcApplication;
import com.wyc.simple.rpc.core.toolkit.LocalServiceCache;
import com.wyc.simple.rpc.core.serializer.Serializer;
import com.wyc.simple.rpc.core.server.base.SimpleRpcRequest;
import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;
import com.wyc.simple.rpc.core.serializer.factory.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * HTTP 请求处理器
 */
@Slf4j
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest httpRequest) {
        // 获取序列化器
        Serializer serializer;
        try {
            serializer = SerializerFactory.getInstance(SimpleRpcApplication.getSimpleRpcConfig().getSerializer());
        } catch (Exception exception) {
            serializer = SerializerFactory.DEFAULT_SERIALIZER;
        }
        final Serializer httpSerializer = serializer;

        // 记录日志
        log.info("[HttpServerHandler 接收并处理请求 method:{} uri:{}]", httpRequest.method(), httpRequest.uri());

        // 处理请求体
        httpRequest.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            SimpleRpcRequest simpleRpcRequest = null;
            try {
                simpleRpcRequest = httpSerializer.deserialize(bytes, SimpleRpcRequest.class);
            } catch (IOException e) {
                log.error("[HttpServerHandler 反序列化失败 detail:{}]", JSON.toJSONString(e));
            }

            // 返回错误情况
            SimpleRpcResponse simpleRpcResponse = new SimpleRpcResponse();
            if (simpleRpcRequest == null) {
                simpleRpcResponse.setMessage("request is null");
                sendResponse(httpRequest, simpleRpcResponse, httpSerializer);
                return;
            }

            try {
                // 查询本地服务
                String serviceName = simpleRpcRequest.getServiceName();
                Object serviceInstance = LocalServiceCache.getBean(serviceName);
                if (serviceInstance == null) {
                    Class<?> serviceClass = LocalServiceCache.getClass(serviceName);
                    if (serviceClass != null) {
                        serviceInstance = serviceClass.getConstructor().newInstance();
                    }
                }
                if (serviceInstance == null) {
                    throw new RuntimeException("【SimpleRpcServerHandler 无法找到服务: " + serviceName + " ]");
                }

                // 调用单例服务对象方法
                String methodName = simpleRpcRequest.getMethodName();
                Class<?>[] parameterTypes = simpleRpcRequest.getParameterTypes();
                Object[] args = simpleRpcRequest.getArgs();
                Method method = serviceInstance.getClass().getMethod(methodName, parameterTypes);
                Object resultData = method.invoke(serviceInstance, args);

                // 设置 rpc 响应
                simpleRpcResponse.setData(resultData);
                simpleRpcResponse.setDataType(method.getReturnType());
                simpleRpcResponse.setMessage("ok");
            } catch (Exception e) {
                log.error("[HttpServerHandler 调用本地方法出错 detail:{}]",  JSON.toJSONString(e));
                simpleRpcResponse.setMessage(e.getMessage());
                simpleRpcResponse.setException(e);
            }

            // 响应
            sendResponse(httpRequest, simpleRpcResponse, httpSerializer);
        });

    }

    /**
     * 返回响应
     * @param httpRequest
     * @param simpleRpcResponse
     * @param serializer
     */
    private void sendResponse(HttpServerRequest httpRequest, SimpleRpcResponse simpleRpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = httpRequest.response()
                .putHeader("content-type", "application/json");
        try {
            httpServerResponse.end(Buffer.buffer(serializer.serialize(simpleRpcResponse)));
        } catch (IOException e) {
            log.error("[HttpServerHandler 返回响应出错 detail:{}]", JSON.toJSONString(e));
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
