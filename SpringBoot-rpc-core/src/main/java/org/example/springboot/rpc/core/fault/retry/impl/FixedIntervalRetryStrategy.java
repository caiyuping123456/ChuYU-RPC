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
 * 固定时间间隔重试策略
 */
@Component
@Retry(KEYS = "fixedInterval")
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {
    /**
     * 固定时间间隔重试策略
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        //定义了这个重试器所操作的任务的返回值类型是 RpcResponse
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                //如果没有异常（触发异常）
                .retryIfExceptionOfType(Exception.class)
                //这是时间间隔
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                        //这个表示最大连接次数是3次
                        .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                        .withRetryListener(new RetryListener() {
                            @ Override
                            public <V> void onRetry(Attempt<V> attempt) {
                                log.info("重试次数 {}", attempt.getAttemptNumber());
                            }
                        })
                        .build();
        return retryer.call(callable);
    }
}
