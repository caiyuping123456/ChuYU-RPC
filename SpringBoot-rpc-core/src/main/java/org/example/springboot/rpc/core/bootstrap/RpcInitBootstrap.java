package org.example.springboot.rpc.core.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.EnableRpc;
import org.example.springboot.rpc.core.config.RpcApplication;
import org.example.springboot.rpc.core.config.RpcConfig;
import org.example.springboot.rpc.core.http.HttpService;
import org.example.springboot.rpc.core.http.VertxHttpServer;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * rpc全局的启动器
 * 在Spring启动时，获取EnableRpc中的属性，进行对应的初始化
 * 这里可以使用以实现 Spring 的
 * ImportBeanDefinitionRegistrar  接口，并且在
 * registerBeanDefinitions  方法中，获取到项目的注解和注解属性。
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring在初始化时，初始化Rpc框架
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //获取Enable注解的属性
        //全限定类名
        boolean needServer = (boolean)importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()).get("needServer");
        RpcApplication.init();

        //全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        //启动服务器
        if (needServer){
            HttpService vertxTcpServer = new VertxHttpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
        }else{
            log.info("不启动server");
        }
    }
}

