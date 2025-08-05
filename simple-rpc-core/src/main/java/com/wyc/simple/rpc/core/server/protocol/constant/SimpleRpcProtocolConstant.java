package com.wyc.simple.rpc.core.server.protocol.constant;

public interface SimpleRpcProtocolConstant {

    /**
     * 消息头长度
     */
    int MESSAGE_HEADER_LENGTH = 17;

    /**
     * 魔数
     */
    byte PROTOCOL_MAGIC = 0x1;

    /**
     * 协议版本
     */
    byte PROTOCOL_VERSION = 0x23;

    /**
     * 协议消息体长度在协议消息头中的偏移量
     */
    int BODY_LENGTH_OFFSET = 13;
}
