package com.wyc.simple.rpc.core.annotation.autoconfig;

import cn.hutool.core.util.StrUtil;
import com.wyc.simple.rpc.core.SimpleRpcApplication;
import com.wyc.simple.rpc.core.config.SimpleRpcConfig;
import com.wyc.simple.rpc.core.server.Server;
import com.wyc.simple.rpc.core.server.impl.VertxHttpServer;
import com.wyc.simple.rpc.core.server.impl.VertxSimpleRpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

/**
 * simple-rpc 启动自动配置类
 * 启动 HTTP 或者 SimpleRpc 服务器
 */
@Slf4j
public class SimpleRpcServerBootstrapAutoConfig implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        SimpleRpcConfig simpleRpcConfig = SimpleRpcApplication.getSimpleRpcConfig();
        String protocol = simpleRpcConfig.getProtocol();
        if (StrUtil.isBlank(protocol)) {
            protocol = "http";
        }

        Server server;
        if ("http".equals(protocol)) {
            server = new VertxHttpServer();
        } else {
            server = new VertxSimpleRpcServer();
        }
        server.doStart(SimpleRpcApplication.getSimpleRpcConfig().getServerPort());
        log.info("[SimpleRpcServerBootstrapAutoConfig Server启动成功]");
    }
}
