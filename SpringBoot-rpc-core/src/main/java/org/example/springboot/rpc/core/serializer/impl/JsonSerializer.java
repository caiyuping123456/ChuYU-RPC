package org.example.springboot.rpc.core.serializer.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springboot.rpc.core.annotation.RpcSerializer;
import org.example.springboot.rpc.core.model.RpcRequest;
import org.example.springboot.rpc.core.model.RpcResponse;
import org.example.springboot.rpc.core.serializer.Serializer;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JSON序列化
 * */

@Component
@RpcSerializer(value = "json",code = 1)
public class JsonSerializer implements Serializer {

    //Json对象（用于Java对象序列化和反序列化的对象）
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    //序列化
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        //通过Json对象对object进行序列化
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    //反序列化
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        //通过JSON对象将字节数组中的序列化为Object
        T obj = OBJECT_MAPPER.readValue(bytes, type);
        //防止类型擦除
        /**
         * 注意，request是请求（rpcRequest）
         * 后端需要获取所有
         * */
        if (obj instanceof RpcRequest) {
            return handleRequest((RpcRequest) obj, type);
        }
        /**
         * 注意，response是响应
         * 所以只需要修改Data值
         * */
        if (obj instanceof RpcResponse){
            return handleResponse((RpcResponse) obj, type);
        }
        return obj;
    }


    private <T> T handleResponse(RpcResponse obj, Class<T> type) throws IOException {
        Object data = obj.getData();
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(data);
        obj.setData(OBJECT_MAPPER.readValue(bytes, obj.getDataType()));
        return type.cast(obj);
    }

    private <T> T handleRequest(RpcRequest obj, Class<T> type) throws IOException {
        //获取参数的类型
        Class<?>[] parameterTypes = obj.getParameterTypes();
        //获取参数
        Object[] args = obj.getArgs();
        //循环处理每个类型
        for(int i=0;i<parameterTypes.length;i++){
            Class<?> clazz = parameterTypes[i];
            //类型不同
            if (!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(bytes, clazz);
            }
        }
        return type.cast(obj);
    }
}
