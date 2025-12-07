package org.example.springboot.rpc.core.fault.retry;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.Retry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RetryStrategyManager implements ApplicationContextAware {

    /**
     * 存储map集合，用于管理重试机制的注解
     */
    private final static Map<String,RetryStrategy> RetryStrategyMap = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;


    /**
     * 注入application依赖
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init(){
        Map<String, RetryStrategy> beansOfType = applicationContext.getBeansOfType(RetryStrategy.class);
        log.info("初始化重试机制中...");
        for (RetryStrategy retryStrategy : beansOfType.values()){
            Retry annotation = retryStrategy.getClass().getAnnotation(Retry.class);
            String key;
            if (annotation==null&&!annotation.KEYS().isEmpty()) key = annotation.KEYS().toLowerCase();
            else key = retryStrategy.getClass().getSimpleName().toLowerCase();
            RetryStrategyMap.put(key, retryStrategy);
        }
        log.info("重试机制初始化完成！");
    }

    public static RetryStrategy getRetryStrategy(String key){
        if (key == null && key.equals("") && !RetryStrategyMap.containsKey(key.toLowerCase())) {
            throw new RuntimeException("重试机制：传入的key值为空或者Map集合中没有这个机制");
        }
        return RetryStrategyMap.get(key.toLowerCase());
    }


}
