package org.example.springboot.rpc.core.annotation;


import org.example.springboot.rpc.core.bootstrap.RpcConsumerBootstrap;
import org.example.springboot.rpc.core.bootstrap.RpcInitBootstrap;
import org.example.springboot.rpc.core.bootstrap.RpcProviderBoostrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于全局标识项目需要引入Rpc，执行初始化
 */
//用于的地方
@Target(ElementType.TYPE)
//保存到
@Retention(RetentionPolicy.RUNTIME)
/**
 * @Import 是 Spring 框架提供的一种配置类组合机制。
 * 当一个配置类（通常用 @Configuration 标记）上使用了 @Import 注解时，
 * Spring 容器会在加载这个配置类时，同时加载并注册所有 @Import 中指定的类。
 */
@Import({RpcProviderBoostrap.class, RpcInitBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {

    /**
     * 需要启动server
     * @return
     */
    boolean needServer() default true;
}
