package org.example.springboot.rpc.core.fault.tolerant.impl;

import org.example.springboot.rpc.core.annotation.Tolerant;
import org.example.springboot.rpc.core.fault.tolerant.TolerantStrategy;
import org.example.springboot.rpc.core.model.RpcResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 故障恢复策略
 */
@Component
@Tolerant(tolerant = "failBack")
public class FailBackTolerantStrategy implements TolerantStrategy {
    /**
     * 故障恢复策略
     * @param context
     * @param e
     * @return
     */
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        //TODO todo 可自行扩展，获取降级的服务并调用
        /**
         * 调用本地实例化
         * 通过反射（Map value 实例化）
         */
        return null;
    }
}
