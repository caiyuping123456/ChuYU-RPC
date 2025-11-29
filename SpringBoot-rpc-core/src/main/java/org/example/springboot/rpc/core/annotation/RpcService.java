package org.example.springboot.rpc.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务提供者使用，用于需要注册或者提供的服务类类上
 * 需要指定服务注册信息属性，比如服务接口实现类、版本号等（也可以包括
 * 服务名称）
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {

    /**
     * 服务接口类
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 版本号
     */
    String serviceVersion() default "1.0";
}
