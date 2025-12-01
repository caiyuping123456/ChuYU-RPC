package org.example.springboot.rpc.core.serializer;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.RpcSerializer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SerializerManager implements ApplicationContextAware {
    private static Map<String,Serializer> serializerMap = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;//上下文对象

    /**
     * 获取ApplicationContext对象
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 在所有Bean实例化，赋值完成后执行
     */
    @PostConstruct
    public void init(){
        log.info("序列化器初始化中..." );
        //获取所有Serializer的实现类
        Map<String, Serializer> beans = applicationContext.getBeansOfType(Serializer.class);
        for (Serializer serializer : beans.values()){
            //查找注解
            RpcSerializer rpcSerializer = serializer.getClass().getAnnotation(RpcSerializer.class);
            String key;
            if (rpcSerializer != null && !rpcSerializer.value().isEmpty()) {
                key = rpcSerializer.value().toLowerCase();
            }else{
                //如果没有注解就使用类名
                key = serializer.getClass().getSimpleName().toLowerCase();
            }
            serializerMap.put(key,serializer);
        }
        log.info("序列化器初始化成功" );
    }
    public static Serializer getSerializer(String type){
        //默认使用json序列化
        if(type==null && type.equals("")&&serializerMap.containsKey(type.toLowerCase())) type="json";
        log.info("正在使用：{}序列化器！", type.toLowerCase());
        return serializerMap.get(type.toLowerCase());
    }


}
