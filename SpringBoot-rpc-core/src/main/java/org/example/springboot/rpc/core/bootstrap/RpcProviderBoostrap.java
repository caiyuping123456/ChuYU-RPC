package org.example.springboot.rpc.core.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.RpcService;
import org.example.springboot.rpc.core.config.RegistryConfig;
import org.example.springboot.rpc.core.config.RpcApplication;
import org.example.springboot.rpc.core.config.RpcConfig;
import org.example.springboot.rpc.core.model.ServiceMetaInfo;
import org.example.springboot.rpc.core.registry.Registry;
import org.example.springboot.rpc.core.registry.RegistryManager;
import org.example.springboot.rpc.core.registry.impl.LocalRegistry;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Rpc服务提供启动类
 * 通过Spring获取所有Bean,同时读取所有Bean上有RpcServer注解的类
 */
@Slf4j
@Component
public class RpcProviderBoostrap implements BeanPostProcessor {
    /**
     * Bean初始化之后，注册服务
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //跳过代理对象
        if(AopUtils.isAopProxy(bean)){
            return bean;
        }
        //获取bean对象
        Class<?> beanClass = bean.getClass();

        //获取这个rpc提供端注解（用于判断这个Bean对象是否被）
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        //判断这个bean有没有这个注解
        if (rpcService != null) {
            //bean有这个注解
            //需要进行注册服务
            //获取接口服务类
            Class<?> aClass = rpcService.interfaceClass();
            //默认值处理
            if (aClass == void.class) {
                //框架自动推断类型
                //这个是取实现的第一个接口
                aClass = beanClass.getInterfaces()[0];
            }
            //获取服务名字
            String serviceName = aClass.getName();
            //服务服务版本
            String serviceVersion = rpcService.serviceVersion();

            //注册服务
            //本地注册
            LocalRegistry.register(serviceName,beanClass);

            //全局配置
            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            //注册服务到注册中心
            //注册中心配置
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            String registryConfigRegistry = registryConfig.getRegistry();
            Registry registry = RegistryManager.getRegistry(registryConfigRegistry);
            //元数据(封装)
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceMetaInfo+"注册失败",e);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
