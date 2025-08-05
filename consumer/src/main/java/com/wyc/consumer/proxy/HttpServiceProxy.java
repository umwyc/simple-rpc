package com.wyc.consumer.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.wyc.simple.rpc.core.SimpleRpcApplication;
import com.wyc.simple.rpc.core.SimpleRpcConstant;
import com.wyc.simple.rpc.core.config.SimpleRpcConfig;
import com.wyc.simple.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.simple.rpc.core.loadbalancer.factory.LoadBalancerFactory;
import com.wyc.simple.rpc.core.server.base.SimpleRpcRequest;
import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;
import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;
import com.wyc.simple.rpc.core.registry.factory.RegistryCenterFactory;
import com.wyc.simple.rpc.core.serializer.Serializer;
import com.wyc.simple.rpc.core.serializer.factory.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 使用 Http 协议进行通信的代理类
 */
@Slf4j
public class HttpServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        // 指定序列化器
        SimpleRpcConfig simpleRpcConfig = SimpleRpcApplication.getSimpleRpcConfig();
        Serializer serializer;
        try {
            serializer = SerializerFactory.getInstance(simpleRpcConfig.getSerializer());
        } catch (Exception e) {
            serializer = SerializerFactory.DEFAULT_SERIALIZER;
            log.error("[HttpServiceProxy 使用默认的序列化器]");
        }

        // 构造请求体
        String serviceName = method.getDeclaringClass().getName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        SimpleRpcRequest simpleRpcRequest = SimpleRpcRequest.builder()
                .serviceName(serviceName)
                .serviceVersion(SimpleRpcConstant.DEFAULT_SERVICE_VERSION)
                .methodName(methodName)
                .parameterTypes(parameterTypes)
                .args(args)
                .build();

        try {
            byte[] bytes = serializer.serialize(simpleRpcRequest);
            byte[] result;
            // 注册中心服务发现
            RegistryCenter registryCenter;
            try {
                registryCenter = RegistryCenterFactory.getInstance(simpleRpcConfig.getRegistryCenterConfig().getRegistry());
            } catch (Exception exception) {
                registryCenter = RegistryCenterFactory.DEFAULT_REGISTRY_CENTER;
                log.error("[HttpServiceProxy 使用默认的注册中心]");
            }
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(SimpleRpcApplication.getSimpleRpcConfig().getVersion());
            List<ServiceMetaInfo> serviceMetaInfoList = registryCenter.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if(CollUtil.isEmpty(serviceMetaInfoList)){
                throw new RuntimeException(String.format("[HttpServiceProxy 注册中心类型为 %s 的注册服务为空]", serviceMetaInfo.getServiceKey()));
            }

            // 使用负载均衡器选择实例
            LoadBalancer loadBalancer;
            try {
                loadBalancer = LoadBalancerFactory.getInstance(simpleRpcConfig.getLoadBalancer());
            } catch (Exception exception) {
                loadBalancer = LoadBalancerFactory.DEFAULT_LOADBALANCER;
                log.error("[HttpServiceProxy 使用默认的负载均衡器]");
            }
            Map<String, Object> parametersMap = buildParametersMap(method, args);
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(parametersMap, serviceMetaInfoList);


            // 使用 Http 协议调用实例所提供的服务
            HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bytes)
                    .execute();
            result = httpResponse.bodyBytes();

            // 获取返回结果
            SimpleRpcResponse simpleRpcResponse = serializer.deserialize(result, SimpleRpcResponse.class);
            return simpleRpcResponse.getData();
        } catch (IOException e) {
            log.error("[HttpServiceProxy 出现IO错误 details:{}]", JSON.toJSONString(e));
        }

        return null;
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
