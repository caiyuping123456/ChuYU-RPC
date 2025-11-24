package org.example.fault.retry;

import org.example.spi.SpiLoader;

/**
 * 重试策略工厂
 */
public class RetryStrategyFactory {

    /**
     * 静态加载
     */
    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试机制
     */
    private final static RetryStrategy DEFAULT_RETRY_STRATEGY =  new NoRetryStrategy();

    public static RetryStrategy getInstance(String key){
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }
}
