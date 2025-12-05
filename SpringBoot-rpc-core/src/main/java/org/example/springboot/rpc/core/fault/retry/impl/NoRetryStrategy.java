package org.example.springboot.rpc.core.fault.retry.impl;

import org.example.springboot.rpc.core.annotation.Retry;
import org.example.springboot.rpc.core.fault.retry.RetryStrategy;
import org.example.springboot.rpc.core.model.RpcResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * 不重试策略
 */
@Component
@Retry(KEYS = "no")
public class NoRetryStrategy implements RetryStrategy {
    /**
     * 这个是不重试
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
