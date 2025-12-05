package org.example.springboot.rpc.core.fault.tolerant;

import org.example.springboot.rpc.core.model.RpcResponse;

import java.util.Map;

/**
 * 快速失效机制----容错机制（本层有错误，立即报告给上层（调用方））
 */
public class FailFastTolerantStrategy implements TolerantStrategy{
    /**
     * 实现方法
     * @param context
     * @param e
     * @return
     */
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException("服务错误",e);
    }
}
