package org.example.springboot.rpc.core.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SpringContextHolder: 用于在非Spring管理代码中获取Spring BeanFactory/Context。
 */
@Component // 确保这个类自身被Spring管理
public class SpringContextHolder implements ApplicationContextAware {

    private static ConfigurableListableBeanFactory BEAN_FACTORY;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 在 Spring 容器初始化完成后，保存 BeanFactory
        if (applicationContext instanceof ConfigurableApplicationContext) {
            BEAN_FACTORY = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        }
    }

    /**
     * 获取 Spring 的 BeanFactory (用于手动注册 Bean)
     */
    public static ConfigurableListableBeanFactory getBeanFactory() {
        if (BEAN_FACTORY == null) {
            throw new IllegalStateException("BeanFactory 尚未初始化。请确保 SpringContextHolder 已被加载。");
        }
        return BEAN_FACTORY;
    }
}