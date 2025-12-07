package org.example.springboot.rpc.core.fault.tolerant;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.Tolerant;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容错机制Bean管理
 */
@Slf4j
@Component
public class TolerantStrategyManager implements ApplicationContextAware {

    /**
     * 管理Bean的Map集合
     */
    private static final Map<String,TolerantStrategy> TolerantStrategys = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;

    /**
     * 初始化
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化Map集合
     */
    @PostConstruct
    public void init(){
        Map<String, TolerantStrategy> beansOfType = applicationContext.getBeansOfType(TolerantStrategy.class);
        log.info("初始化容错机制中....");
        for(TolerantStrategy tolerantStrategy : beansOfType.values()){
            //获取注解
            Tolerant tolerant = tolerantStrategy.getClass().getAnnotation(Tolerant.class);
            String key;
            if (tolerant!=null&&!tolerant.tolerant().isEmpty()) key = tolerant.tolerant().toLowerCase();
            else key = tolerantStrategy.getClass().getSimpleName().toLowerCase();
            TolerantStrategys.put(key, tolerantStrategy);
        }
        log.info("容错机制初始化完成！");
    }

    public static TolerantStrategy getTolerantStrategy(String key){
        if(key==null&&key.equals("")&&!TolerantStrategys.containsKey(key.toLowerCase())) {
            throw new RuntimeException("容错机制：传入的key为空或者key值对应的Bean对象为注入");
        }
        return TolerantStrategys.get(key.toLowerCase());
    }
}
