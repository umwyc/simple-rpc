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
    byte PROTOCOL_VERSION = 0x1;
}
