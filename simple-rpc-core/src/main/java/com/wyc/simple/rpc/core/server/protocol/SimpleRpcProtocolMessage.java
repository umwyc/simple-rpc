package com.wyc.simple.rpc.core.server.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义消息（协议）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleRpcProtocolMessage<T> {

    /**
     * 协议消息头
     */
    private Header header;

    /**
     * 消息体
     */
    private T body;

    /**
     * 消息头
     */
    @Data
    public static class Header{
        /**
         * 魔数，保证安全性
         */
        private byte magic;

        /**
         * 协议版本号
         */
        private byte version;

        /**
         * 序列化器
         */
        private byte serializer;

        /**
         * 类型
         */
        private byte type;

        /**
         * 状态
         */
        private byte status;

        /**
         * 请求 id
         */
        private long requestId;

        /**
         * 消息体长度
         */
        private int bodyLength;
    }
}
