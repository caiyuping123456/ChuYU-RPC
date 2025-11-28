package org.example.rpc.springbootstart.annotation;

import org.example.constant.RpcConstant;
import org.example.fault.retry.RetryStrategyKeys;
import org.example.fault.tolerant.TolerantStrategyKeys;
import org.example.loadbalancer.LoadBalancerKeys;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消费者使用注解，里面包含接口服务类，版本号，均衡策略，重试策略，容错策略等
 */

@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {

    /**
     * 接口服务类
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 版本号
     */
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 负载均衡器
     */
    String loadBalancer() default LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 容错策略
     */
    String tolerantStrategy() default TolerantStrategyKeys.FAIL_FAST;

    /**
     * 重试策略
     */
    String retryStrategy() default RetryStrategyKeys.NO;

    /**
     * 模拟调用
     */
    boolean mock() default false;

}
