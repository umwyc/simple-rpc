package com.wyc.simple.rpc.core.demo;

import com.wyc.simple.rpc.core.server.Server;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

/**
 * 服务端实例代码
 */
public class VertxTcpServerDemo implements Server {

    private byte[] handleRequest(byte[] requestBytes){
        return "Hello Client".getBytes();
    }

    @Override
    public void doStart(int port) {
        // 创建 TCP 服务器
        Vertx vertx = Vertx.vertx();
        NetServer netServer = vertx.createNetServer();

        // 处理连接
        netServer.connectHandler(socket -> {
            // 处理请求
            socket.handler(request -> {
                // 接收请求
                byte[] requestBytes = request.getBytes();
                System.out.println("[VertxTcpServerDemo 接收到客户端请求: " + new String(requestBytes) + " ]");

                // 返回响应
                byte[] responseBytes = handleRequest(requestBytes);
                socket.write(Buffer.buffer(responseBytes));
            });
        });

        // 启动服务器并监听端口
        netServer.listen(port, "localhost", result -> {
           if(result.succeeded()){
               System.out.println("[VertxTcpServerDemo 服务器启动成功，正在监听端口: " + port + " ]");
           }else{
               System.out.println("[VertxTcpServerDemo 服务器启动失败]");
           }
        });
    }

    public static void main(String[] args) {
        VertxTcpServerDemo vertxTcpServerDemo = new VertxTcpServerDemo();
        vertxTcpServerDemo.doStart(8888);
    }
}
