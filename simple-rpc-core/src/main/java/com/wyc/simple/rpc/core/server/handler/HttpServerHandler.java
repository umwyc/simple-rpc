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
                String serviceName = simpleRpcRequest.getServiceName();
                String methodName = simpleRpcRequest.getMethodName();
                Class<?>[] parameterTypes = simpleRpcRequest.getParameterTypes();
                Object[] args = simpleRpcRequest.getArgs();
                // 查询本地缓存
                Class<?> serviceClass = LocalServiceCache.get(serviceName);
                // 调用本地方法
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                Object object = method.invoke(serviceClass.getConstructor().newInstance(), args);
                simpleRpcResponse.setData(object);
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
