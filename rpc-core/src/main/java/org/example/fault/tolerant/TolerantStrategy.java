package org.example.fault.tolerant;

import org.example.model.RpcResponse;

import java.util.Map;

/**
 * 容错机制的接口方法，如果用户需要自己写就必须要实现这个接口
 */
public interface TolerantStrategy {
    /**
     * 接口方法
     * @param context
     * @param e
     * @return
     */
    RpcResponse doTolerant(Map<String,Object> context,Exception e);
}
