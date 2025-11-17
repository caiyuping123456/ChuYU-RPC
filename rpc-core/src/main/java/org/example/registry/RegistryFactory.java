package org.example.registry;

import org.example.spi.SpiLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册中心工厂(用于获取注册中心对象)
 */
public class RegistryFactory {
    //单例模式
    private static final Map<String, Registry> CACHE = new ConcurrentHashMap<>();

    static{
        SpiLoader.load(Registry.class);
    }
    /**
     * 默认注册中心
     */
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 根据key获取实例
     */
    public static Registry getInstance(String key){
        //当Map中没有时调用这个getInstance，同时存入map中
        return CACHE.computeIfAbsent(key, k -> SpiLoader.getInstance(Registry.class, k));
//        return SpiLoader.getInstance(Registry.class, key);
    }
}
