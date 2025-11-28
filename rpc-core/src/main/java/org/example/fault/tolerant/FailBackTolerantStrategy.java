package org.example.fault.tolerant;

import org.example.model.RpcResponse;

import java.util.Map;

/**
 * 故障恢复策略
 */
public class FailBackTolerantStrategy implements TolerantStrategy{
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
