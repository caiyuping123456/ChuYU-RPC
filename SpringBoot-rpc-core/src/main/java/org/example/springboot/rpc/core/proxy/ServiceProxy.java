package org.example.springboot.rpc.core.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.example.springboot.rpc.core.config.RpcApplication;
import org.example.springboot.rpc.core.config.RpcConfig;
import org.example.springboot.rpc.core.model.RpcRequest;
import org.example.springboot.rpc.core.model.RpcResponse;
import org.example.springboot.rpc.core.utils.JDKSerializer;
import org.example.springboot.rpc.core.utils.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * 服务代理（JDK 动态代理）
 */
public class ServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws IOException {
        // 指定序列化器
        Serializer serializer = new JDKSerializer();
        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            //application
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 发送请求
            try (HttpResponse httpResponse = HttpRequest.post(getUrl(rpcConfig.getServerHost(),rpcConfig.getServerPort()))
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用动态加载Url
     * @param host
     * @param port
     * @return
     */
    private String getUrl(String host,Integer port){
        return "http://"+host+":"+port.toString();
    }
}