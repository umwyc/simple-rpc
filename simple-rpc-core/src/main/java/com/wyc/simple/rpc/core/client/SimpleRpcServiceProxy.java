package com.wyc.simple.rpc.core.client;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.wyc.simple.rpc.core.SimpleRpcApplication;
import com.wyc.simple.rpc.core.SimpleRpcConstant;
import com.wyc.simple.rpc.core.config.SimpleRpcConfig;
import com.wyc.simple.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.simple.rpc.core.loadbalancer.factory.LoadBalancerFactory;
import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;
import com.wyc.simple.rpc.core.registry.factory.RegistryCenterFactory;
import com.wyc.simple.rpc.core.retry.RetryStrategy;
import com.wyc.simple.rpc.core.retry.factory.RetryStrategyFactory;
import com.wyc.simple.rpc.core.server.base.SimpleRpcBufferHandlerWrapper;
import com.wyc.simple.rpc.core.server.base.SimpleRpcRequest;
import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;
import com.wyc.simple.rpc.core.server.protocol.ProtocolMessageDecoder;
import com.wyc.simple.rpc.core.server.protocol.ProtocolMessageEncoder;
import com.wyc.simple.rpc.core.server.protocol.SimpleRpcProtocolMessage;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolConstant;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolMessageSerializerEnum;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolMessageStatusEnum;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolMessageTypeEnum;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 使用 SimpleRpc 协议进行通信的代理类
 */
@Slf4j
public class SimpleRpcServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        SimpleRpcConfig simpleRpcConfig = SimpleRpcApplication.getSimpleRpcConfig();

        // 构造请求头
        SimpleRpcProtocolMessage.Header header = new SimpleRpcProtocolMessage.Header();
        header.setMagic(SimpleRpcProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(SimpleRpcProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) SimpleRpcProtocolMessageSerializerEnum.getEnumByValue(SimpleRpcApplication.getSimpleRpcConfig().getSerializer()).getKey());
        header.setType((byte) SimpleRpcProtocolMessageTypeEnum.REQUEST.getKey());
        header.setStatus((byte) SimpleRpcProtocolMessageStatusEnum.OK.getValue());
        header.setRequestId(IdUtil.getSnowflakeNextId());

        // 构造请求体
        Class<?> serviceClass = method.getDeclaringClass();
        String serviceName = serviceClass.getName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        SimpleRpcRequest simpleRpcRequest = SimpleRpcRequest.builder()
                .serviceName(serviceName)
                .serviceVersion(SimpleRpcConstant.DEFAULT_SERVICE_VERSION)
                .methodName(methodName)
                .parameterTypes(parameterTypes)
                .args(args)
                .build();

        // 构造请求
        SimpleRpcProtocolMessage<SimpleRpcRequest> simpleRpcProtocolMessageRequest = new SimpleRpcProtocolMessage<>();
        simpleRpcProtocolMessageRequest.setHeader(header);
        simpleRpcProtocolMessageRequest.setBody(simpleRpcRequest);

        // 注册中心服务发现
        RegistryCenter registryCenter;
        try {
            registryCenter = RegistryCenterFactory.getInstance(simpleRpcConfig.getRegistryCenterConfig().getRegistry());
        } catch (Exception exception) {
            registryCenter = RegistryCenterFactory.DEFAULT_REGISTRY_CENTER;
            log.error("[SimpleServiceProxy 使用默认的注册中心]");
        }
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(SimpleRpcApplication.getSimpleRpcConfig().getVersion());
        List<ServiceMetaInfo> serviceMetaInfoList = registryCenter.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if(CollUtil.isEmpty(serviceMetaInfoList)){
            throw new RuntimeException(String.format("[SimpleRpcServiceProxy 注册中心类型为 %s 的注册服务为空]", serviceMetaInfo.getServiceKey()));
        }

        // 使用负载均衡器选择实例
        LoadBalancer loadBalancer;
        try {
            loadBalancer = LoadBalancerFactory.getInstance(simpleRpcConfig.getLoadBalancer());
        } catch (Exception exception) {
            loadBalancer = LoadBalancerFactory.DEFAULT_LOADBALANCER;
            log.error("[SimpleRpcServiceProxy 使用默认的负载均衡器]");
        }
        Map<String, Object> parametersMap = buildParametersMap(method, args);
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(parametersMap, serviceMetaInfoList);

        // 获取重试策略
        RetryStrategy retryStrategy;
        try {
            retryStrategy = RetryStrategyFactory.getInstance(SimpleRpcApplication.getSimpleRpcConfig().getRetryStrategy());
        } catch (Exception exception) {
            retryStrategy = RetryStrategyFactory.DEFAULT_RETRY_STRATEGY;
            log.error("[SimpleRpcServiceProxy 使用默认的重试策略]");
        }

        // 创建 SimpleRpc 客户端
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();

        // 客户端发送请求
        SimpleRpcResponse simpleRpcResponse = retryStrategy.retry(() -> {

            CompletableFuture<SimpleRpcResponse> responseFuture = new CompletableFuture<>();

            netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getServiceHost(), result -> {
                if (result.succeeded()) {
                    log.info("[SimpleRpcServiceProxy 客户端连接 {} 主机成功]", serviceMetaInfo.getServiceAddress());

                    NetSocket socket = result.result();

                    try {
                        socket.write(ProtocolMessageEncoder.encode(simpleRpcProtocolMessageRequest));
                    } catch (IOException e) {
                        throw new RuntimeException(String.format("[SimpleRpcServiceProxy 客户端发送消息失败 details:%s]", JSON.toJSONString(e)));
                    }

                    // 处理响应结果
                    SimpleRpcBufferHandlerWrapper simpleRpcBufferHandlerWrapper = new SimpleRpcBufferHandlerWrapper(response -> {
                        try {
                            SimpleRpcProtocolMessage<SimpleRpcResponse> simpleRpcProtocolMessage
                                    = (SimpleRpcProtocolMessage<SimpleRpcResponse>) ProtocolMessageDecoder.decode(response);
                            responseFuture.complete(simpleRpcProtocolMessage.getBody());
                        } catch (IOException e) {
                            throw new RuntimeException(String.format("[SimpleRpcServiceProxy 客户端解码响应失败 details:%s]", JSON.toJSONString(e)));
                        }
                    });
                    socket.handler(simpleRpcBufferHandlerWrapper);
                } else {
                    log.info("[SimpleRpcServiceProxy 客户端启动失败]");
                }
            });

            // 关闭连接
            netClient.close();
            // 获取返回结果
            return responseFuture.get();
        });

        return simpleRpcResponse.getData();
    }

    /**
     * 构建 Map
     * @param method
     * @param args
     * @return
     */
    private Map<String, Object> buildParametersMap(Method method, Object[] args) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Class<?> parameterType :method.getParameterTypes()) {
            resultMap.put(parameterType.getName(), args);
        }
        return resultMap;
    }
}
