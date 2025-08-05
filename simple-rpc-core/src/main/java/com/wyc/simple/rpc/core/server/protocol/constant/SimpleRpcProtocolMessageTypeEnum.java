package com.wyc.simple.rpc.core.server.protocol.constant;

import lombok.Getter;

/**
 * 协议消息类型
 */
@Getter
public enum SimpleRpcProtocolMessageTypeEnum {

    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3);

    private final int key;

    SimpleRpcProtocolMessageTypeEnum(int key) {
        this.key = key;
    }

    public static SimpleRpcProtocolMessageTypeEnum getEnumByKey(int key){
        for (SimpleRpcProtocolMessageTypeEnum value : SimpleRpcProtocolMessageTypeEnum.values()) {
            if(value.key == key){
                return value;
            }
        }
        return null;
    }
}
