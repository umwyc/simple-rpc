package com.wyc.simple.rpc.core.retry.impl;

import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;
import com.wyc.simple.rpc.core.retry.RetryStrategy;

import java.util.concurrent.Callable;

/**
 * 不重试
 */
public class NoRetryStrategy implements RetryStrategy {

    @Override
    public SimpleRpcResponse retry(Callable<SimpleRpcResponse> callable) throws Exception {
        return callable.call();
    }
}
