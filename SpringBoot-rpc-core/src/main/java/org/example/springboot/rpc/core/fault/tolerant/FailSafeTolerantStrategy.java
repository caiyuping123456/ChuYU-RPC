package org.example.springboot.rpc.core.fault.tolerant;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.model.RpcResponse;

import java.util.Map;

/**
 * 静默容错机制----（不做什么抛异常，打印一个日志，同时放回一个正常结果）
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy{
    /**
     * 打印一个日志，同时返回一个正常结果
     * @param context
     * @param e
     * @return
     */
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("服务错误：", e);
        return new RpcResponse();
    }
}
