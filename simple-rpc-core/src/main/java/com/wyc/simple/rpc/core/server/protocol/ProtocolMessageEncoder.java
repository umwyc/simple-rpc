package com.wyc.simple.rpc.core.server.protocol;

import com.alibaba.fastjson.JSON;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolMessageSerializerEnum;
import com.wyc.simple.rpc.core.serializer.Serializer;
import com.wyc.simple.rpc.core.serializer.factory.SerializerFactory;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 自定义消息编码器
 */
@Slf4j
public class ProtocolMessageEncoder {

    public static Buffer encode(SimpleRpcProtocolMessage<?> simpleRpcProtocolMessage) throws IOException {
        if(simpleRpcProtocolMessage == null || simpleRpcProtocolMessage.getHeader() == null){
            log.warn("[ProtocolMessageEncoder 消息编码器: SimpleRpcProtocolMessage 为空或者 Header 为空 details:{}]"
                    , JSON.toJSONString(simpleRpcProtocolMessage));
            return Buffer.buffer();
        }

        SimpleRpcProtocolMessage.Header header = simpleRpcProtocolMessage.getHeader();
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());

        // 获取序列化器
        SimpleRpcProtocolMessageSerializerEnum serializerEnum = SimpleRpcProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if(serializerEnum == null){
            throw new RuntimeException("[ProtocolMessageEncoder 指定的序列化器不存在]");
        }
        Serializer serializer;
        try {
            serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        } catch (Exception e) {
            log.info("[ProtocolMessageEncoder 使用默认的序列化器]");
            serializer = SerializerFactory.DEFAULT_SERIALIZER;
        }

        // 序列化消息体
        byte[] bodyBytes = serializer.serialize(simpleRpcProtocolMessage.getBody());
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);
        return buffer;
    }
}
