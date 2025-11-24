package org.example;

import org.example.fault.retry.ExponentialBackoffRetryStrategy;
import org.example.fault.retry.FixedIntervalRetryStrategy;
import org.example.fault.retry.NoRetryStrategy;
import org.example.fault.retry.RetryStrategy;
import org.example.model.RpcResponse;
import org.junit.Test;

public class RetryStrategyTest {

    /**
     * 使用重试一次
     */
    //RetryStrategy retryStrategy = new NoRetryStrategy();

    /**
     * 使用间隔重试
     */
//    RetryStrategy retryStrategy = new FixedIntervalRetryStrategy();

    /**
     * 指数退避重试机制
     */
    RetryStrategy retryStrategy = new ExponentialBackoffRetryStrategy();

    @Test
    public void doRetry() {
        try {
            RpcResponse rpcResponse = retryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败");
            });
            System.out.println(rpcResponse);
        } catch (Exception e) {
            System.out.println("重试多次失败");
            e.printStackTrace();
        }
    }
}
