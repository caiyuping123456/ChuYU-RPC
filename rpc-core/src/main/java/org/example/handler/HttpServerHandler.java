package org.example.handler;

import cn.hutool.core.bean.BeanUtil;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.example.RpcApplication;
import org.example.model.RpcRequest;
import org.example.model.RpcResponse;
import org.example.model.ServiceMetaInfo;
import org.example.registry.LocalRegistry;
import org.example.registry.Registry;
import org.example.registry.RegistryFactory;
import org.example.serializer.Serializer;
import org.example.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        //指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        //记录日志
        System.out.println("Received request: "+httpServerRequest.method() );

        //异步处理HTTP请求
        httpServerRequest.body(body ->{
            byte[] bytes = body.result().getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            //请求为null;
            if (rpcRequest == null) {
                rpcResponse.setMessage("null");
                doResponse(httpServerRequest,rpcResponse,serializer);
                return ;
            }
            //不是null，解析方法
            try {
                //这里是获取本地注册中心的注册服务类
                //这里可以进行判断，先判断本地注册中心中有没有，没有的话进行远程调用
                Class<?> aClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = aClass.getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
                Object object = method.invoke(aClass.newInstance(), rpcRequest.getArgs());
                rpcResponse.setData(object);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (Exception e){
                //打印异常
                e.printStackTrace();
            }
            //响应
            doResponse(httpServerRequest,rpcResponse,serializer);
        });
    }

    private void doResponse(HttpServerRequest httpServerRequest, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = httpServerRequest.response()
                .putHeader("content-type", "application/json");
        try {
            // 序列化
            byte[] serialized = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
