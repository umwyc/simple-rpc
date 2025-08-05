package com.wyc.simple.rpc.core.server.protocol;

import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolConstant;
import com.wyc.simple.rpc.core.server.base.SimpleRpcRequest;
import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolMessageSerializerEnum;
import com.wyc.simple.rpc.core.server.protocol.constant.SimpleRpcProtocolMessageTypeEnum;
import com.wyc.simple.rpc.core.serializer.Serializer;
import com.wyc.simple.rpc.core.serializer.factory.SerializerFactory;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 自定义消息解码器
 */
@Slf4j
public class ProtocolMessageDecoder {

    public static SimpleRpcProtocolMessage<?> decode(Buffer buffer) throws IOException {
        // 校验魔数
        byte magic = buffer.getByte(0);
        if(magic != SimpleRpcProtocolConstant.PROTOCOL_MAGIC){
            throw new RuntimeException(String.format("[ProtocolMessageDecoder 消息 magic=%s 非法]", (int) magic));
        }
        // 读取 Header
        SimpleRpcProtocolMessage.Header header = new SimpleRpcProtocolMessage.Header();
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));

        // 获取指定的序列化器
        SimpleRpcProtocolMessageSerializerEnum serializerEnum = SimpleRpcProtocolMessageSerializerEnum.getEnumByKey(buffer.getByte(2));
        if(serializerEnum == null){
            throw new RuntimeException("[ProtocolMessageDecoder 指定的序列化器不存在]");
        }
        Serializer serializer;
        try {
            serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        } catch (Exception exception) {
            log.info("[ProtocolMessageDecoder 使用默认的序列化器]");
            serializer = SerializerFactory.DEFAULT_SERIALIZER;
        }

        // 解码消息体
        int bodyLength = buffer.getInt(13);
        byte[] bodyBytes = buffer.getBytes(17, 17 + bodyLength);
        byte type = buffer.getByte(3);
        SimpleRpcProtocolMessageTypeEnum typeEnum = SimpleRpcProtocolMessageTypeEnum.getEnumByKey(type);
        if(typeEnum == null){
            throw new RuntimeException(String.format("[ProtocolMessageDecoder type=%s 的消息类型不存在]", (int) type));
        }
        return switch (typeEnum) {
            case REQUEST -> {
                SimpleRpcRequest simpleRpcRequest = serializer.deserialize(bodyBytes, SimpleRpcRequest.class);
                yield new SimpleRpcProtocolMessage<>(header, simpleRpcRequest);
            }
            case RESPONSE -> {
                SimpleRpcResponse simpleRpcResponse = serializer.deserialize(bodyBytes, SimpleRpcResponse.class);
                yield new SimpleRpcProtocolMessage<>(header, simpleRpcResponse);
            }
            default -> throw new RuntimeException("[ProtocolMessageDecoder 暂不支持该消息类型]");
        };
    }
}
