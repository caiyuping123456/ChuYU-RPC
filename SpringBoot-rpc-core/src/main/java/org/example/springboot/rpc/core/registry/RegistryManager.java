package org.example.springboot.rpc.core.registry;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.RpcRegistry;
import org.example.springboot.rpc.core.config.RpcApplication;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RegistryManager implements ApplicationContextAware {

    /**
     * 用于存储注册中心选择
     */
    private final static Map<String,Registry> registryManager = new ConcurrentHashMap<>();

    /**
     * 上下文对象
     */
    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 注册中心初始化
     */
    @PostConstruct
    public void init(){
        log.info("注册中心初始化中....");
        //获取所有实现Registry的实现类
        Map<String, Registry> beansOfType = applicationContext.getBeansOfType(Registry.class);
        for(Registry registry : beansOfType.values()){
            //获取有这个RpcRegistry注解的
            RpcRegistry annotation = registry.getClass().getAnnotation(RpcRegistry.class);
            String key;
            if (annotation!=null&&!annotation.value().isEmpty()) {
                key = annotation.value().toLowerCase();
            }else {
                //没有就使用默类名为key值
                key = registry.getClass().getSimpleName().toLowerCase();
            }
            registryManager.put(key,registry);
        }
        log.info("注册中心初始化成功");
    }

    public static Registry getRegistry(String type){
        //默认使用etcd注册中心
        if (type==null&&type.equals("")&&registryManager.containsKey(type.toLowerCase())) {
            type = "etcd";
        }
        Registry registry = registryManager.get(type.toLowerCase());
        //init初始化（用于连接）
        registry.init(RpcApplication.getRpcConfig().getRegistryConfig());
        return registry;
    }
}
