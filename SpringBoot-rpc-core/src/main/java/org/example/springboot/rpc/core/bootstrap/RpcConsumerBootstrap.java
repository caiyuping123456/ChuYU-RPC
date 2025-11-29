package org.example.springboot.rpc.core.bootstrap;

import org.example.springboot.rpc.core.annotation.RpcReference;
import org.example.springboot.rpc.core.proxy.ServiceProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * Rpc 服务消费者启动
 */
public class RpcConsumerBootstrap implements BeanPostProcessor {

    /**
     * Bean 初始化后执行，注入服务
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //获取类的class类
        Class<?> aClass = bean.getClass();
        //遍历类的属性
        Field[] fields = aClass.getDeclaredFields();
        for(Field field : fields){
            //判断是否有这个注解
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            //为空（消费者用户没有指定接口class）
            if (rpcReference != null) {
                //生成代理对象
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if (interfaceClass == void.class) {
                    //默认使用字段本身的类型
                    interfaceClass = field.getType();
                }
                //允许访问和修改私有字段
                field.setAccessible(true);
                Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass);
                try {
                    field.set(bean,proxyObject);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("为字段注入代理对象失败",e);
                }
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
