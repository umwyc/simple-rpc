package com.wyc.provider;

import com.alibaba.fastjson.JSON;
import com.wyc.common.service.OrderService;
import com.wyc.common.service.UserService;
import com.wyc.provider.impl.OrderServiceImpl;
import com.wyc.simple.rpc.core.SimpleRpcApplication;
import com.wyc.simple.rpc.core.config.RegistryCenterConfig;
import com.wyc.simple.rpc.core.config.SimpleRpcConfig;
import com.wyc.simple.rpc.core.toolkit.LocalServiceCache;
import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.registry.base.ServiceMetaInfo;
import com.wyc.simple.rpc.core.registry.factory.RegistryCenterFactory;
import com.wyc.simple.rpc.core.server.Server;
import com.wyc.simple.rpc.core.server.impl.VertxHttpServer;
import com.wyc.provider.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * 提供者启动类
 */
@Slf4j
public class HttpProvider {
    public static void main(String[] args) {
        // 初始化 SimpleRpc 框架
        SimpleRpcApplication.init();

        // 缓存本地服务
        LocalServiceCache.register(UserService.class.getName(), UserServiceImpl.class);
        LocalServiceCache.register(OrderService.class.getName(), OrderServiceImpl.class);

        // 推送服务至注册中心
        SimpleRpcConfig simpleRpcConfig = SimpleRpcApplication.getSimpleRpcConfig();
        RegistryCenterConfig registryCenterConfig = simpleRpcConfig.getRegistryCenterConfig();
        RegistryCenter registryCenter = RegistryCenterFactory.getInstance(registryCenterConfig.getRegistry());

        ServiceMetaInfo userServiceMetaInfo = new ServiceMetaInfo();
        String userServiceName = UserService.class.getName();
        userServiceMetaInfo.setServiceName(userServiceName);
        userServiceMetaInfo.setServiceVersion(simpleRpcConfig.getVersion());
        userServiceMetaInfo.setServiceHost(simpleRpcConfig.getServerHost());
        userServiceMetaInfo.setServicePort(simpleRpcConfig.getServerPort());

        ServiceMetaInfo orderServiceMetaInfo = new ServiceMetaInfo();
        String orderServiceName = OrderService.class.getName();
        orderServiceMetaInfo.setServiceName(orderServiceName);
        orderServiceMetaInfo.setServiceVersion(simpleRpcConfig.getVersion());
        orderServiceMetaInfo.setServiceHost(simpleRpcConfig.getServerHost());
        orderServiceMetaInfo.setServicePort(simpleRpcConfig.getServerPort());

        try {
            registryCenter.register(userServiceMetaInfo);
            registryCenter.register(orderServiceMetaInfo);
        } catch (Exception e) {
            log.error("[HttpProvider 推送服务至注册中心失败 details:{}]", JSON.toJSONString(e));
            throw new RuntimeException(e);
        }

        // 启动服务器
        Server server = new VertxHttpServer();
        server.doStart(simpleRpcConfig.getServerPort());
    }
}
