package com.wyc.simple.rpc.core.server.protocol.constant;

import lombok.Getter;

/**
 * 协议消息状态
 */
@Getter
public enum SimpleRpcProtocolMessageStatusEnum {

    OK("ok", 20),
    BAD_REQUEST("badRequest", 40),
    BAD_RESPONSE("badResponse", 50);

    private final String text;

    private final int value;

    SimpleRpcProtocolMessageStatusEnum(String text, Integer value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取 enum
     * @param value
     * @return
     */
    public static SimpleRpcProtocolMessageStatusEnum getEnumByValue(int value){
        for (SimpleRpcProtocolMessageStatusEnum simpleRpcProtocolMessageStatusEnum : SimpleRpcProtocolMessageStatusEnum.values()) {
            if(simpleRpcProtocolMessageStatusEnum.getValue() == value){
                return simpleRpcProtocolMessageStatusEnum;
            }
        }
        return null;
    }
}
