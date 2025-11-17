package org.example.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class SpiLoader {

    private static final Logger log = LoggerFactory.getLogger(SpiLoader.class);

    // 缓存：接口全限定名 -> (key -> 实现类 Class)
    private static final Map<String, Map<String, Class<?>>> LOADER_MAP = new ConcurrentHashMap<>();

    // 记录已加载的接口类型，避免重复加载
    private static final Set<String> LOADED_INTERFACES = ConcurrentHashMap.newKeySet();

    private SpiLoader() {}

    /**
     * 获取 SPI 实例（懒加载）
     */
    public static <T> T getInstance(Class<T> tClass, String key) {
        String tClassName = tClass.getName();

        // 第一次检查：是否已加载该接口
        if (!LOADED_INTERFACES.contains(tClassName)) {
            synchronized (SpiLoader.class) {
                // 双重检查
                if (!LOADED_INTERFACES.contains(tClassName)) {
                    load(tClass);
                    LOADED_INTERFACES.add(tClassName);
                }
            }
        }

        Map<String, Class<?>> keyClassMap = LOADER_MAP.get(tClassName);
        if (keyClassMap == null || !keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format(
                    "SpiLoader 的 %s 不存在 key=%s 的实现", tClassName, key));
        }
        Class<?> aClass = keyClassMap.get(key);
        try {
            return tClass.cast(aClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载指定接口的所有实现类（仅内部调用）
     */
    public static void load(Class<?> loadClass) {
        String resourcePath = "META-INF/rpc/system/" + loadClass.getName();
        log.info("Lazy loading SPI for: {}", loadClass.getName());

        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                log.warn("SPI resource not found: {}", resourcePath);
                LOADER_MAP.put(loadClass.getName(), Collections.emptyMap());
                return;
            }

            Properties properties = new Properties();
            properties.load(inputStream);

            Map<String, Class<?>> keyClassMap = new HashMap<>();
            for (String key : properties.stringPropertyNames()) {
                String className = properties.getProperty(key);
                try {
                    Class<?> implClass = Class.forName(className);
                    // 校验是否是 loadClass 的子类
                    if (!loadClass.isAssignableFrom(implClass)) {
                        log.warn("Class {} is not assignable from {}", className, loadClass.getName());
                        continue;
                    }
                    keyClassMap.put(key, implClass);
                } catch (ClassNotFoundException e) {
                    log.error("SPI implementation class not found: {}", className, e);
                }
            }

            LOADER_MAP.put(loadClass.getName(), keyClassMap);
        } catch (IOException e) {
            log.error("Failed to load SPI resource: {}", resourcePath, e);
            LOADER_MAP.put(loadClass.getName(), Collections.emptyMap());
        }
    }
}

//
///**
// * SPI 加载器（支持键值对映射）
// */
//@Slf4j
//public class SpiLoader {
//
//    /**
//     * 存储已加载的类：接口名 =>（key => 实现类）
//     */
//    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();
//
//    /**
//     * 对象实例缓存（避免重复 new），类路径 => 对象实例，单例模式
//     */
//    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();
//
//    /**
//     * 系统 SPI 目录
//     */
//    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";
//
//    /**
//     * 用户自定义 SPI 目录
//     */
//    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";
//
//    /**
//     * 扫描路径(系统放前面，用户可以覆盖系统)
//     */
//    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR,RPC_CUSTOM_SPI_DIR};
//
//    /**
//     * 动态加载的类列表（示例：需要你自己根据实际替换）
//     */
//    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(
//            // 示例：Serialization.class, LoadBalancer.class 等
//            //这里是启动时加载所有默认的spi，这里时系统写的序列化实现类
//            Serializer.class
//    );
//
//    /**
//     * 加载所有类型
//     */
//    public static void loadAll() {
//        log.info("加载所有 SPI");
//        //注意这里是加载动态加载的类
//        for (Class<?> aClass : LOAD_CLASS_LIST) {
//            load(aClass);
//        }
//    }
//
//    /**
//     * 获取某个接口的实例
//     *
//     * @param tClass 接口类型
//     * @param key    配置文件中的 key
//     * @param <T>    泛型
//     * @return 实例对象
//     */
//    @SuppressWarnings("unchecked")
//    public static <T> T getInstance(Class<?> tClass, String key) {
//        //key是对应的spi标识，比如：JsonSerializer对应的key是json
//        String tClassName = tClass.getName();
//        //从存储中加载对应的接口的实现类
//        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
//        if (keyClassMap == null) {
//            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型", tClassName));
//        }
//        //判断这个接口的实现类没有这个key对应的实现类
//        if (!keyClassMap.containsKey(key)) {
//            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key=%s 的实现", tClassName, key));
//        }
//
//        // 获取到要加载的实现类型
//        Class<?> implClass = keyClassMap.get(key);
//
//        // 从实例缓存中加载指定类型的实例
//        String implClassName = implClass.getName();
//        /**
//         * 如果 instanceCache 中 没有 implClassName 对应的实例：
//         * 执行 lambda 表达式（即创建新实例）
//         * 将结果存入缓存，并返回
//         * 如果 已有，则直接返回已有值，不会执行 lambda
//         * */
//        instanceCache.computeIfAbsent(implClassName, k -> {
//            try {
//                return implClass.getDeclaredConstructor().newInstance();
//            } catch (Exception e) {
//                String errorMsg = String.format("%s 类实例化失败", implClassName);
//                throw new RuntimeException(errorMsg, e);
//            }
//        });
//
//        return (T) instanceCache.get(implClassName);
//    }
//
//    /**
//     * 加载某个类型
//     *
//     * @param loadClass 要加载的接口类
//     * @return key -> 实现类的映射
//     */
//    public static Map<String, Class<?>> load(Class<?> loadClass) {
//        log.info("加载类型为 {} 的 SPI", loadClass.getName());
//        // 扫描路径，用户自定义的 SPI 优先级高于系统 SPI（custom 在前）
//        Map<String, Class<?>> keyClassMap = new HashMap<>();
//        for (String scanDir : SCAN_DIRS) {
//
//                /***
//                 * try {
//                 *           //target目录下可能有多个同名的URL，所以返回一个枚举类
//                 *                 Enumeration<URL> resources = SpiLoader.class.getClassLoader()
//                 *                         .getResources(scanDir + loadClass.getName());
//                 *
//                 *                 //遍历枚举中的URL
//                 *                 while (resources.hasMoreElements()) {
//                 *                     URL resource = resources.nextElement();
//                 *                     //通过输入流读入URL对应文件的内容
//                 *                     try (InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
//                 *                          BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
//                 *                         String line;
//                 *                         while ((line = bufferedReader.readLine()) != null) {
//                 *                             line = line.trim();
//                 *                             if (line.isEmpty() || line.startsWith("#")) {
//                 *                                 continue; // 跳过空行和注释
//                 *                             }
//                 *                             String[] strArray = line.split("=", 2); // 最多分成两部分
//                 *                             if (strArray.length == 2) {
//                 *                                 String key = strArray[0].trim();
//                 *                                 String className = strArray[1].trim();
//                 *                                 // 自定义配置会覆盖系统配置（因为 custom 先加载，但这里后出现的会覆盖？）
//                 *                                 // 如果希望 custom 优先，应该先加载 system，再加载 custom（或者反过来处理顺序）
//                 *                                 //将key和对应的类存储到map中
//                 *                                 keyClassMap.put(key, Class.forName(className));
//                 *                             }
//                 *                         }
//                 *                     }
//                 *                 } catch (IOException | ClassNotFoundException e) {
//                 *                 log.error("spi resource load error for class: {}", loadClass.getName(), e);
//                 *             }
//                 */
//            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
//            for (URL resource : resources) {
//                try {
//                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
//                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//                    String line;
//                    while ((line = bufferedReader.readLine()) != null) {
//                        String[] strArray = line.split("=");
//                        if (strArray.length > 1) {
//                            String key = strArray[0];
//                            String className = strArray[1];
//                            keyClassMap.put(key, Class.forName(className));
//                        }
//                    }
//                } catch (Exception e) {
//                    log.error("spi resource load error", e);
//                }
//            }
//        }
//        loaderMap.put(loadClass.getName(), keyClassMap);
//        return keyClassMap;
//    }
//}