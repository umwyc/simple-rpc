package com.wyc.simple.rpc.core.server.protocol.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 协议使用的序列化器
 */
@Getter
public enum SimpleRpcProtocolMessageSerializerEnum {

    JDK(0, "jdk"),
    JSON(1, "json"),
    KRYO(2, "kryo"),
    HESSIAN(3, "hessian");

    private final int key;

    private final String value;

    SimpleRpcProtocolMessageSerializerEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 获取 values
     * @return
     */
    public static List<String> getValues(){
        return Arrays.stream(SimpleRpcProtocolMessageSerializerEnum.values())
                .map(item -> item.value)
                .collect(Collectors.toList());
    }

    /**
     * 通过 key 获取 enum
     * @param key
     * @return
     */
    public static SimpleRpcProtocolMessageSerializerEnum getEnumByKey(int key){
        for(SimpleRpcProtocolMessageSerializerEnum item : SimpleRpcProtocolMessageSerializerEnum.values()){
            if(item.key == key){
                return item;
            }
        }
        return null;
    }

    /**
     * 通过 value 获取 enum
     * @param value
     * @return
     */
    public static SimpleRpcProtocolMessageSerializerEnum getEnumByValue(String value){
        for(SimpleRpcProtocolMessageSerializerEnum item : SimpleRpcProtocolMessageSerializerEnum.values()){
            if(item.value.equals(value)){
                return item;
            }
        }
        return null;
    }
}
