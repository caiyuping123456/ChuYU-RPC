package org.example.proxy;

import org.example.RpcApplication;
import org.example.config.RpcConfig;

import java.lang.reflect.Proxy;

/**
 *
 服务代理工厂（用于创建代理对象）
 */
public class ServiceProxyFactory {
    /**
     *
     根据服务类获取代理对象
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        //判断配置文件是否设置默认
        if(RpcApplication.getRpcConfig().isMock()){
            return getMockProxy(serviceClass);
        }

        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());
    }

    /**
     * 获取Mock
     * */
    private static <T> T getMockProxy(Class<T> serviceClass){
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy());
    }
}