package org.example.springboot.rpc.core.serializer;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.RpcSerializer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SerializerManager implements ApplicationContextAware {
    private final static Map<String,Serializer> serializerMap = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;//上下文对象

    private static Map<String,Integer> protocolMessageSerializerEnum = new ConcurrentHashMap<>();

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
            Integer code = null;
            if (rpcSerializer != null && !rpcSerializer.value().isEmpty()) {
                key = rpcSerializer.value().toLowerCase();
                code = rpcSerializer.code();
                if (protocolMessageSerializerEnum.containsValue(code)) {
                    throw new RuntimeException("您的自定义序列化code必须从4开始，依次递增不可重复");
                }
            }else{
                //如果没有注解就使用类名
                key = serializer.getClass().getSimpleName().toLowerCase();
                // 如果没有预设值，给一个错误码或自动分配
                if (code == null) {
                    throw new RuntimeException("未注解的序列化器 [" + key + "] 必须在静态配置中定义 code。");
                }
            }
            serializerMap.put(key,serializer);
            protocolMessageSerializerEnum.put(key,code);
        }
        log.info("序列化器初始化成功" );
    }
    public static Serializer getSerializer(String type){
        //默认使用json序列化
        if(type==null && type.equals("")&&serializerMap.containsKey(type.toLowerCase())) type="json";
        log.info("正在使用：{}序列化器！", type.toLowerCase());
        return serializerMap.get(type.toLowerCase());
    }

    public static Integer getProtocolMessageSerializerEnumByKey(String key){
        if (key==null&&key.equals("")&&!protocolMessageSerializerEnum.containsKey(key)){
            throw new RuntimeException("传入的key值为空或者map中没有这个key");
        }
        return protocolMessageSerializerEnum.get(key);
    }

    public static String getProtocolMessageSerializerEnumByValue(Integer value){
        if (value==null&&!protocolMessageSerializerEnum.containsValue(value)){
            throw new RuntimeException("传入的value值为空或者map中没有这个value");
        }
        // 遍历 Map 中的所有条目 (Entry)
        for (Map.Entry<String, Integer> entry : protocolMessageSerializerEnum.entrySet()) {
            // 检查当前条目的值 (Value) 是否与目标值匹配
            // 使用 equals() 确保 Integer 对象的正确比较
            if (entry.getValue().equals(value)) {
                // 如果找到匹配的值，返回对应的键 (Key)
                return entry.getKey();
            }
        }
        throw new RuntimeException("传入的key值为空或者map中没有这个key");
    }


}
