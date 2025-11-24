package org.example.fault.retry;

/**
 * 重试策略键名常量
 */
public interface RetryStrategyKeys {
    /**
     * 不重试策略
     */
    String NO = "no";
    /**
     * 固定间隔重试策略
     */
    String FIXED_INTERVAL = "fixedInterval";
    /**
     * 下面还可以加自己实现的重试策略
     */

    /**
     * 指数退避重试策略
     */
    String EXPONENTIALBACKOFF = "exponentialBackoff";

}
