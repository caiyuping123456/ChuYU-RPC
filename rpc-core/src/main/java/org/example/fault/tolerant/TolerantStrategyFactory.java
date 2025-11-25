package org.example.fault.tolerant;


import org.example.spi.SpiLoader;

/**
 * 容错策略工厂
 */
public class TolerantStrategyFactory {

    /**
     * 初始化策略（SPI）
     */
    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    /**
     * 默认策略(快速失效策略)
     */
    private final static TolerantStrategy tolerantStrategy = new FailFastTolerantStrategy();

    /**
     * 配置文件初始化
     */

    public static TolerantStrategy getInstance(String key){
        return SpiLoader.getInstance(TolerantStrategy.class,key);
    }
}
