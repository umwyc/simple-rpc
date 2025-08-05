package com.wyc.simple.rpc.core.retry.factory;

import com.wyc.simple.rpc.core.retry.RetryStrategy;
import com.wyc.simple.rpc.core.retry.impl.NoRetryStrategy;
import com.wyc.simple.rpc.core.spi.SpiLoader;

/**
 * 重试策略工厂
 */
public class RetryStrategyFactory {

    static{
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试策略
     */
    public static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * 从工厂中获取实例
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key){
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }
}
