package org.example.fault.retry;

import org.example.model.RpcResponse;
import java.util.concurrent.Callable;


/**
 * 重试策略
 * 就是将TPC进行包装
 * 用 Guava-Retrying 库来实现重试
 */
public interface RetryStrategy {
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
