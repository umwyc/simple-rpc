package com.wyc.simple.rpc.core.demo;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户端实例代码
 */
public class VertxTcpClientDemo {

    public static void main(String[] args) {
        // 创建 TCP 客户端
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();

//        // 使用客户端发送一个请求
//        netClient.connect(8888, "localhost", result -> {
//            if (result.succeeded()) {
//                System.out.println("[VertxTcpClientDemo 客户端启动成功]");
//                NetSocket socket = result.result();
//                // 使用客户端发送请求
//                socket.write(Buffer.buffer("Hello Server"));
//                socket.handler(response -> {
//                    byte[] responseBytes = response.getBytes();
//                    System.out.println("[VertxTcpClientDemo 接收到服务端响应响应: " + new String(responseBytes) + "]");
//                });
//
//            }else{
//                System.out.println("[VertxTcpClientDemo 客户端启动失败]");
//            }
//        });

        // 使用客户端连续发送请求（半包、粘包）
        AtomicLong count = new AtomicLong(0);
        netClient.connect(8888, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("[VertxTcpClientDemo 客户端启动成功]");

                NetSocket socket = result.result();

                // 绑定响应处理器
                socket.handler(response -> {
                    byte[] responseBytes = response.getBytes();
                    System.out.println("[VertxTcpClientDemo 接收到服务端响应响应: " + new String(responseBytes) + "]");
                });

                // 使用客户端连续发送请求
                while (true) {
                    socket.write(Buffer.buffer("Hello Server " + count.incrementAndGet()));
                }
            }else{
                System.out.println("[VertxTcpClientDemo 客户端启动失败]");
            }
        });

    }
}
