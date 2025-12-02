package org.example.springboot.rpc.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface RpcLoadBalancer {
    /**
     * 负载均衡的别称
     *  一定要加
     */
    String loadBalancer();
}
