package org.example.springboot.rpc.core.fault.tolerant.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.Tolerant;
import org.example.springboot.rpc.core.fault.tolerant.TolerantStrategy;
import org.example.springboot.rpc.core.model.RpcResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 静默容错机制----（不做什么抛异常，打印一个日志，同时放回一个正常结果）
 */
@Slf4j
@Component
@Tolerant(tolerant = "failSaft")
public class FailSafeTolerantStrategy implements TolerantStrategy {
    /**
     * 打印一个日志，同时返回一个正常结果
     * @param context
     * @param e
     * @return
     */
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("安全失效：服务错误：", e);
        return new RpcResponse();
    }
}
