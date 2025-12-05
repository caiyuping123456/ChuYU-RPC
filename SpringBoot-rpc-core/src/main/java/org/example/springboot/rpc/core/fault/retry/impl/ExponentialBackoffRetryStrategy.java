package org.example.springboot.rpc.core.fault.retry.impl;


import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.Retry;
import org.example.springboot.rpc.core.fault.retry.RetryStrategy;
import org.example.springboot.rpc.core.model.RpcResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 指数退避重试策略
 * 同样是调用Guava-Retrying库进行实现
 */
@Component
@Retry(KEYS = "exponentialBackoff")
@Slf4j
public class ExponentialBackoffRetryStrategy implements RetryStrategy {

    /**
     * 指数退避算法
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                //如果没有异常时执行
                .retryIfExceptionOfType(Exception.class)
                //初始等待时间100L,指数因子5L
                .withWaitStrategy(WaitStrategies.exponentialWait(100L, 5L, TimeUnit.SECONDS))
                //重试5次
                .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                //监听
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("任务重试：第 {} 次尝试失败", attempt.getAttemptNumber());
                    }
                }).build();
        //调用
        return retryer.call(callable);
    }
}
