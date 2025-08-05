package com.wyc.simple.rpc.core.serializer.factory;

import com.wyc.simple.rpc.core.serializer.Serializer;
import com.wyc.simple.rpc.core.serializer.impl.JdkSerializer;
import com.wyc.simple.rpc.core.spi.SpiLoader;

/**
 * 序列化器工厂
 */
public class SerializerFactory {

    static {
       SpiLoader.load(Serializer.class);
    }

    /**
     * 默认的序列化器
     */
    public static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Serializer getInstance(String key){
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
