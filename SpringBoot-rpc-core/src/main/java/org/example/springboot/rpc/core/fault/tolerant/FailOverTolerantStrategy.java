package org.example.springboot.rpc.core.fault.tolerant;

import org.example.springboot.rpc.core.model.RpcResponse;

import java.util.Map;

/**
 * 故障转移策略
 */
public class FailOverTolerantStrategy implements TolerantStrategy {
    /**
     * 故障转移策略
     * @param context
     * @param e
     * @return
     */
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // todo 可自行扩展，获取其他服务节点并调用
        return null;
    }
}
