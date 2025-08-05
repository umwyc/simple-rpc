package com.wyc.simple.rpc.core.server.handler;

import com.wyc.simple.rpc.core.server.base.SimpleRpcRequest;
import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;
import com.wyc.simple.rpc.core.server.protocol.SimpleRpcProtocolMessage;
import com.wyc.simple.rpc.core.server.protocol.ProtocolMessageDecoder;
import com.wyc.simple.rpc.core.server.protocol.ProtocolMessageEncoder;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolMessageTypeEnum;
import com.wyc.simple.rpc.core.toolkit.LocalServiceCache;
import com.wyc.simple.rpc.core.server.base.SimpleRpcBufferHandlerWrapper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 使用 SimpleRpc 协议的请求处理器
 */
public class SimpleRpcServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket socket) {
        // 处理请求
        SimpleRpcBufferHandlerWrapper simpleRpcBufferHandlerWrapper = new SimpleRpcBufferHandlerWrapper(request -> {
            // 解码
            SimpleRpcProtocolMessage<SimpleRpcRequest> simpleRpcProtocolMessageRequest;
            try {
                simpleRpcProtocolMessageRequest = (SimpleRpcProtocolMessage<SimpleRpcRequest>) ProtocolMessageDecoder.decode(request);
            } catch (IOException e) {
                throw new RuntimeException("[SimpleRpcServerHandler 解析协议失败]");
            }
            SimpleRpcRequest simpleRpcRequest = simpleRpcProtocolMessageRequest.getBody();

            // 找到本地服务，并通过反射机制调用本地服务
            String serviceName = simpleRpcRequest.getServiceName();
            Class<?> implClass = LocalServiceCache.get(serviceName);
            SimpleRpcResponse simpleRpcResponse = new SimpleRpcResponse();
            try {
                Method method = implClass.getMethod(simpleRpcRequest.getMethodName(), simpleRpcRequest.getParameterTypes());
                Object object = method.invoke(implClass.newInstance(), simpleRpcRequest.getArgs());
                // 设置rpc响应
                simpleRpcResponse.setData(object);
                simpleRpcResponse.setDataType(method.getReturnType());
                simpleRpcResponse.setMessage("ok");
            } catch (Exception e) {
                // 设置rpc响应
                e.printStackTrace();
                simpleRpcResponse.setException(e);
                simpleRpcResponse.setMessage(e.getMessage());
            }

            // 编码并响应
            SimpleRpcProtocolMessage<SimpleRpcResponse> simpleRpcProtocolMessageResponse = new SimpleRpcProtocolMessage<>();
            // 将消息类型设置为响应类型
            simpleRpcProtocolMessageRequest.getHeader().setType((byte) SimpleRpcProtocolMessageTypeEnum.RESPONSE.getKey());
            simpleRpcProtocolMessageResponse.setHeader(simpleRpcProtocolMessageRequest.getHeader());
            simpleRpcProtocolMessageResponse.setBody(simpleRpcResponse);

            try {
                Buffer buffer = ProtocolMessageEncoder.encode(simpleRpcProtocolMessageResponse);
                socket.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException("服务端协议编码失败");
            }

        });
        socket.handler(simpleRpcBufferHandlerWrapper);
    }
}
