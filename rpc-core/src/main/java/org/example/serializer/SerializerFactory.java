package org.example.serializer;


import org.example.spi.SpiLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializerFactory {

//    /**
//     * 序列化映射（用于实现单例）
//     */
//    private static final Map<String, Serializer> KEY_SERIALIZOR_MAP = new HashMap<String, Serializer>() {{
//        put(SerializerKeys.JDK, new JDKSerializer());
//        put(SerializerKeys.JSON, new JsonSerializer());
//        put(SerializerKeys.KRYO, new KryoSerializer());
//        put(SerializerKeys.HESSIAN, new HessianSerializer());
//    }};
//
//    /**
//     * 默认序列化器
//     */
//    private static final Serializer DEFAULT_SERIALIZER = KEY_SERIALIZOR_MAP.get(SerializerKeys.JSON);
//
//    /**
//     * 获取实例
//     *
//     * @param key 序列化方式的键（如 "json", "kryo"）
//     * @return 对应的 Serializer 实例，若不存在则返回默认
//     */
//    public static Serializer getInstance(String key) {
//        return KEY_SERIALIZOR_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
//    }

//    static {
//        //默认加载
//        SpiLoader.load(Serializer.class);
//    }
//    /**
//     *
//     默认序列化器
//     */
//    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();
//    /**
//     *
//     获取实例
//     *
//     * @param key
//     * @return
//     */
//    public static Serializer getInstance(String key) {
//        return SpiLoader.getInstance(Serializer.class, key);
//    }

    //懒加载

    // 懒加载缓存：key -> 实例
    private static final Map<String, Serializer> INSTANCE_CACHE = new ConcurrentHashMap<>();

    // 私有构造防止外部实例化
    private SerializerFactory() {}

    /**
     * 懒加载获取序列化器实例
     */
    public static Serializer getInstance(String key) {
        //判断，如果key对应的实例没有就加载
        return INSTANCE_CACHE.computeIfAbsent(key, k -> {
            // 通过 SPI 获取对应的 Class
            Serializer serializerClass = SpiLoader.getInstance(Serializer.class, k);
            try {
                return serializerClass;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create serializer instance: " + k, e);
            }
        });
    }
}