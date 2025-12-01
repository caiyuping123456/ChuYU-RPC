package org.example.springboot.rpc.core.registry.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.config.SpringContextHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LocalRegistry {
    /**
     *
     注册信息存储
     */
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();
    /**
     *
     注册服务
     *
     * @param serviceName
     * @param implClass
     */
    public static void register(String serviceName, Class<?> implClass) {
        if(map.containsKey(serviceName)){
            return ;
        }
        map.put(serviceName, implClass);
        log.info("服务实现类 " + implClass.getName() + " 已注册到 Spring IoC 容器，Bean 名称: " + serviceName);
    }
    /**
     *
     获取服务
     *
     * @param serviceName
     * @return
     */
    public static Class<?> get(String serviceName) {
        return map.get(serviceName);
    }
    /**
     *
     删除服务
     *
     * @param serviceName
     */
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }
}
