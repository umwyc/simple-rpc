package com.wyc.simple.rpc.core.server.impl;

import com.wyc.simple.rpc.core.server.handler.HttpServerHandler;
import com.wyc.simple.rpc.core.server.Server;
import io.vertx.core.Vertx;

/**
 * 使用HTTP协议的服务器实现类
 */
public class VertxHttpServer implements Server {
    @Override
    public void doStart(int port) {
        // 创建 Vertx Http 服务器
        Vertx vertx = Vertx.vertx();
        io.vertx.core.http.HttpServer httpServer = vertx.createHttpServer();

        // 绑定处理器
        httpServer.requestHandler(new HttpServerHandler());

        // 启动服务器并监听端口
        httpServer.listen(port, result->{
           if(result.succeeded()) {
               System.out.println("[Vertx HttpServer is listening on port " + port + " ]");
           }else {
               System.err.println("[Vertx HttpServer Failed to listen on port " + port + " ]");
           }
        });
    }
}
