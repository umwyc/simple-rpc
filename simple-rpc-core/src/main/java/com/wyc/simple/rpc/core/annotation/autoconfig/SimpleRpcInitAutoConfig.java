package com.wyc.simple.rpc.core.annotation.autoconfig;

import com.wyc.simple.rpc.core.SimpleRpcApplication;
import com.wyc.simple.rpc.core.annotation.EnableSimpleRpc;
import com.wyc.simple.rpc.core.server.Server;
import com.wyc.simple.rpc.core.server.impl.VertxHttpServer;
import com.wyc.simple.rpc.core.server.impl.VertxSimpleRpcServer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * simple-rpc 启动自动配置类
 */
public class SimpleRpcInitAutoConfig implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        Map<String, Object> map = annotationMetadata.getAnnotationAttributes(EnableSimpleRpc.class.getName());
        if (map == null) {
            throw new RuntimeException("[SimpleRpcBootstrapAutoConfig @EnableSimpleRpc 注册不存在]");
        }
        String protocol = (String) map.get("protocol");
        if (protocol == null) {
            throw new RuntimeException("[SimpleRpcBootstrapAutoConfig @EnableSimpleRpc 没有指定 protocol]");
        }

        // 初始化 simple-rpc 框架配置
        SimpleRpcApplication.init();

        // 启动服务器
        Server server = null;
        if (protocol.equals("simple-rpc")) {
             server = new VertxSimpleRpcServer();
        } else if (protocol.equals("http")) {
            server = new VertxHttpServer();
        }
        if (server == null) {
            throw new RuntimeException("[SimpleRpcBootstrapAutoConfig @EnableSimpleRpc 指定 protocol 不合法]");
        }
        server.doStart(SimpleRpcApplication.getSimpleRpcConfig().getServerPort());
    }

}
