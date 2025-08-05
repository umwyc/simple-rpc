package com.wyc.simple.rpc.core.retry.impl;

import com.github.rholder.retry.*;
import com.wyc.simple.rpc.core.server.base.SimpleRpcResponse;
import com.wyc.simple.rpc.core.retry.RetryStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔重试
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    @Override
    public SimpleRpcResponse retry(Callable<SimpleRpcResponse> callable) throws Exception {
        Retryer<SimpleRpcResponse> retryer = RetryerBuilder.<SimpleRpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))   // 重试时间间隔为 3s
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))   // 最大重试次数为 3 次
                .withRetryListener(new RetryListener() {    //绑定重试的监听器记录重试次数的日志
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber() - 1);
                    }
                }).build();
        return retryer.call(callable);
    }
}
