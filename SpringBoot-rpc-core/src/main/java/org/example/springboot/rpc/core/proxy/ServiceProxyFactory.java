package org.example.springboot.rpc.core.proxy;

import org.example.springboot.rpc.core.config.RpcApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

/**
 *
 服务代理工厂（用于创建代理对象）
 */
@Component
public class ServiceProxyFactory {
    /**
     *
     根据服务类获取代理对象
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    @Bean
    public static <T> T getProxy(Class<T> serviceClass) {

        if (RpcApplication.getRpcConfig().isMock()) {
            return getMockProxy(serviceClass);
        }

        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());
    }

    public static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy());
    }


}