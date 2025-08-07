package com.wyc.simple.rpc.core.server.handler;

import com.alibaba.fastjson.JSON;
import com.wyc.simple.rpc.core.server.base.SimpleRpcRequest;
import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;
import com.wyc.simple.rpc.core.server.protocol.SimpleRpcProtocolMessage;
import com.wyc.simple.rpc.core.server.protocol.ProtocolMessageDecoder;
import com.wyc.simple.rpc.core.server.protocol.ProtocolMessageEncoder;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolConstant;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolMessageStatusEnum;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolMessageTypeEnum;
import com.wyc.simple.rpc.core.toolkit.LocalServiceCache;
import com.wyc.simple.rpc.core.server.base.SimpleRpcBufferHandlerWrapper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 使用 SimpleRpc 协议的请求处理器
 */
@Slf4j
public class SimpleRpcServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket socket) {
        // 处理请求
        SimpleRpcBufferHandlerWrapper simpleRpcBufferHandlerWrapper = new SimpleRpcBufferHandlerWrapper(buffer -> {
            // 解码
            SimpleRpcProtocolMessage<SimpleRpcRequest> simpleRpcProtocolMessageRequest;
            try {
                simpleRpcProtocolMessageRequest = (SimpleRpcProtocolMessage<SimpleRpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("[SimpleRpcServerHandler 解析协议失败]");
            }

            SimpleRpcRequest simpleRpcRequest = simpleRpcProtocolMessageRequest.getBody();
            boolean isSuccess = true;
            SimpleRpcResponse simpleRpcResponse = new SimpleRpcResponse();
            try {
                // 查询本地服务
                String serviceName = simpleRpcRequest.getServiceName();
                Object serviceInstance = LocalServiceCache.getBean(serviceName);
                if (serviceInstance == null) {
                    Class<?> serviceClass = LocalServiceCache.getClass(serviceName);
                    if (serviceClass != null) {
                        serviceInstance = serviceClass.getConstructor().newInstance();
                    }
                }
                if (serviceInstance == null) {
                    throw new RuntimeException("【SimpleRpcServerHandler 无法找到服务: " + serviceName + " ]");
                }

                // 调用单例服务对象方法
                String methodName = simpleRpcRequest.getMethodName();
                Class<?>[] parameterTypes = simpleRpcRequest.getParameterTypes();
                Object[] args = simpleRpcRequest.getArgs();
                Method method = serviceInstance.getClass().getMethod(methodName, parameterTypes);
                Object resultData = method.invoke(serviceInstance, args);

                // 设置 rpc 响应
                simpleRpcResponse.setData(resultData);
                simpleRpcResponse.setDataType(method.getReturnType());
                simpleRpcResponse.setMessage("ok");
            } catch (Exception e) {
                // 设置 rpc 响应
                log.error("[SimpleRpcServerHandler 调用本地方法出错 detail:{}]",  JSON.toJSONString(e));
                isSuccess = false;
                simpleRpcResponse.setException(e);
                simpleRpcResponse.setMessage(e.getMessage());
            }

            // 设置响应头及响应
            SimpleRpcProtocolMessage<SimpleRpcResponse> simpleRpcProtocolMessageResponse = new SimpleRpcProtocolMessage<>();
            SimpleRpcProtocolMessage.Header header = new SimpleRpcProtocolMessage.Header();
            header.setMagic(SimpleRpcProtocolConstant.PROTOCOL_MAGIC);
            header.setVersion(SimpleRpcProtocolConstant.PROTOCOL_VERSION);
            header.setSerializer(simpleRpcProtocolMessageRequest.getHeader().getSerializer());
            header.setType((byte)SimpleRpcProtocolMessageTypeEnum.RESPONSE.getKey());
            header.setStatus(isSuccess
                    ? (byte)SimpleRpcProtocolMessageStatusEnum.OK.getValue()
                    : (byte)SimpleRpcProtocolMessageStatusEnum.BAD_RESPONSE.getValue());
            simpleRpcProtocolMessageResponse.setHeader(header);
            simpleRpcProtocolMessageResponse.setBody(simpleRpcResponse);

            try {
                // 编码并发送
                Buffer responseBuffer = ProtocolMessageEncoder.encode(simpleRpcProtocolMessageResponse);
                socket.write(responseBuffer);
            } catch (IOException e) {
                throw new RuntimeException("[SimpleRpcServerHandler 服务端协议编码失败]");
            }
        });

        socket.handler(simpleRpcBufferHandlerWrapper);
    }
}
