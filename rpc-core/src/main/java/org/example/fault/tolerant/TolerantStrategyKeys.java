package org.example.fault.tolerant;

/**
 * 用于SPI加载需要的容错策略
 */
public interface TolerantStrategyKeys {
    /**
     * 静默策略
     */
    String FAIL_SAFT = "failSaft";
    /**
     * 故障恢复
     */
    String FAIL_BACK = "failBack";
    /**
     * 故障转移
     */
    String FAIL_OVER = "failOver";
    /**
     * 快速实现策略
     */
    String FAIL_FAST = "failFast";
}
