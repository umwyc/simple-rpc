package com.wyc.simple.rpc.core.server.impl;

import com.wyc.simple.rpc.core.server.handler.SimpleRpcServerHandler;
import com.wyc.simple.rpc.core.server.Server;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

/**
 * 使用 SimpleRpc 协议的服务器实现类
 */
public class VertxSimpleRpcServer implements Server {

    @Override
    public void doStart(int port) {
        // 创建 Vertx SimpleRpc 服务器
        Vertx vertx = Vertx.vertx();
        NetServer netServer = vertx.createNetServer();

        // 绑定处理器
        netServer.connectHandler(new SimpleRpcServerHandler());

        // 启动服务器并监听端口
        netServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is listening on port " + port);
            }else{
                System.out.println("Failed to listen on port " + port);
            }
        });
    }
}
