package org.example.springboot.rpc.core.handler;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.example.springboot.rpc.core.model.RpcRequest;
import org.example.springboot.rpc.core.model.RpcResponse;
import org.example.springboot.rpc.core.registry.LocalRegistry;
import org.example.springboot.rpc.core.utils.JDKSerializer;
import org.example.springboot.rpc.core.utils.Serializer;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        //指定序列化器
        final Serializer serializer = new JDKSerializer();

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
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
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
