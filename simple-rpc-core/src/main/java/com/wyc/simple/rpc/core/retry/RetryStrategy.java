package com.wyc.simple.rpc.core.retry;

import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试机制接口（消费者使用）
 */
public interface RetryStrategy {

    /**
     * 重试
     * @param callable
     * @return
     * @throws Exception
     */
    SimpleRpcResponse retry(Callable<SimpleRpcResponse> callable) throws Exception;

}
