package com.wyc.simple.rpc.core.retry.constant;

/**
 * 重试策略键名常量
 */
public interface RetryStrategyKeys {

    /**
     * 不重试
     */
    String NO = "no";

    /**
     * 固定时间间隔重试
     */
    String FIXED_INTERVAL = "fixedInterval";

    /**
     * 指数时间间隔重试
     */
    String EXPONENTIAL_INTERVAL = "exponentialInterval";

}
