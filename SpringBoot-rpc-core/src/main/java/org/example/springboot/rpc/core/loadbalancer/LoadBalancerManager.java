package org.example.springboot.rpc.core.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.example.springboot.rpc.core.annotation.RpcLoadBalancer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class LoadBalancerManager implements ApplicationContextAware {

    /**
     * 这个是负载均衡的快速匹配Map
     */
    static final private  Map<String,LoadBalancer> loadBalancerMap = new ConcurrentHashMap();
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 表示在初始化之后调用这个函数
     */
    @PostConstruct
    public void init(){
        //获取LoadBalancer的所有子类
        Map<String, LoadBalancer> beansOfType = applicationContext.getBeansOfType(LoadBalancer.class);
        log.info("负载均衡器初始化中...");
        for(LoadBalancer loadBalancer : beansOfType.values()){
            RpcLoadBalancer annotation = loadBalancer.getClass().getAnnotation(RpcLoadBalancer.class);
            String key;
            //表示有注解，同时给注解中的值进行了赋值
            if(annotation != null && !annotation.loadBalancer().isEmpty()) key = annotation.loadBalancer().toLowerCase();
            // 如果没有注解或者有注解同时没有给值赋值
            else key = loadBalancer.getClass().getSimpleName().toString();
            loadBalancerMap.put(key,loadBalancer);
        }
        log.info("负载均衡器初始化成功！");
    }

    public static LoadBalancer getLoadBalancer(String key){
        if (key==null&&key.equals("")) {
            throw new RuntimeException("application.yml文件中负载均衡器为空");
        }
        String type = key.toLowerCase();
        if (!loadBalancerMap.containsKey(type)){
            throw new RuntimeException("没有这个负载均衡器，请自行编写");
        }
        log.info("正在使用"+key+"负载均衡器");
        return loadBalancerMap.get(type);
    }
}
