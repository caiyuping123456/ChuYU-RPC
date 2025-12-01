package org.example.springboot.rpc.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 指定为序列化实现类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcSerializer {

    /**
     * 定义该序列化器的唯一名称，作为框架 Map 的 Key
     */
    String value();
}