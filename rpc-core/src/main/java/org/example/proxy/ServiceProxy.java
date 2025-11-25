package org.example.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import org.example.Enum.ProtocolMessageSerializerEnum;
import org.example.Enum.ProtocolMessageTypeEnum;
import org.example.RpcApplication;
import org.example.config.RegistryConfig;
import org.example.config.RpcConfig;
import org.example.constant.ProtocolConstant;
import org.example.constant.RpcConstant;
import org.example.fault.retry.RetryStrategyFactory;
import org.example.fault.tolerant.TolerantStrategy;
import org.example.fault.tolerant.TolerantStrategyFactory;
import org.example.http.tcp.VertxTcpClient;
import org.example.loadbalancer.LoadBalancer;
import org.example.loadbalancer.LoadBalancerFactory;
import org.example.model.RpcRequest;
import org.example.model.RpcResponse;
import org.example.model.ServiceMetaInfo;
import org.example.protocol.ProtocolMessage;
import org.example.protocol.ProtocolMessageDecoder;
import org.example.protocol.ProtocolMessageEncoder;
import org.example.registry.Registry;
import org.example.registry.RegistryFactory;
import org.example.serializer.Serializer;
import org.example.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 服务代理（JDK 动态代理）
 */
public class ServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     *
     * @return 这个是我们需要的调用对象
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws IOException {
        // 指定序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        String serviceName = method.getDeclaringClass().getName();
        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        // 序列化请求体
        byte[] bodyBytes = serializer.serialize(rpcRequest);

        // 获取全局 RPC 配置
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();

        // 通过注册中心类型（如 "zookeeper,etcd"）获取 Registry 实例
        //单例
        Registry registry = RpcApplication.getRegistry();

        // 构造服务元信息用于服务发现
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        System.out.println("serviceMetaInfo"+serviceMetaInfo);

        // 从注册中心发现可用的服务实例列表
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo);
        //如果所有节点为空，抛出异常
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("暂无可用的服务地址: " + serviceName);
        }

        // 暂时选择第一个服务实例（后续可替换为负载均衡策略）
//            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        // 将调用方法名（请求路径）作为负载均衡参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
            /*// 发送 HTTP 请求并处理响应
            // 根据注册中心中的服务端的信息向服务端发送请求
            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bodyBytes)
                    .execute()) {
                //获取到请求
                byte[] result = httpResponse.bodyBytes();
                // 反序列化响应
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }*/

        //封装
        //return VertxTcpClient.doRequest(rpcRequest,selectedServiceMetaInfo).getData();
        RpcResponse rpcResponse = null;
        try{

            /**
             * 这里直接对TCP进行封装
             * 重试策略
             */
            rpcResponse = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy()).doRetry(() -> {
                return VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            });
        }catch (Exception e){
            /**
             * 容错策略
             */
            System.out.println("容错机制");
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }
        return rpcResponse.getData();
          /*  //使用TCP
            Vertx vertx = Vertx.vertx();
            NetClient netClient = vertx.createNetClient();
            CompletableFuture<RpcResponse> rpcResponseCompletableFuture = new CompletableFuture<>();
            netClient.connect(selectedServiceMetaInfo.getServicePort(),selectedServiceMetaInfo.getServiceHost(),result->{
                //如果发送成功
                if (result.succeeded()) {
                    System.out.println("Connected to TCP server");
                    NetSocket socket = result.result();
                    //发送消息
                    //构造消息
                    ProtocolMessage<RpcRequest> rpcResponseProtocolMessage = new ProtocolMessage<>();
                    ProtocolMessage.Header header = new ProtocolMessage.Header();
                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                    //通过配置文件进行指定序列化
                    header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                    header.setRequestId(IdUtil.getSnowflakeNextId());

                    rpcResponseProtocolMessage.setHeader(header);
                    rpcResponseProtocolMessage.setBody(rpcRequest);
                    //编码请求
                    try{
                        Buffer encodeBuffer = ProtocolMessageEncoder.encode(rpcResponseProtocolMessage);
                        socket.write(encodeBuffer);
                    } catch (IOException e) {
                        throw new RuntimeException("协议消息编码失败",e);
                    }

                    //接受响应
                    socket.handler(buffer -> {
                        try {
                            ProtocolMessage<RpcResponse> rpcResponseDecode = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                            rpcResponseCompletableFuture.complete(rpcResponseDecode.getBody());
                        } catch (IOException e) {
                            throw new RuntimeException("协议消息解码失败",e);
                        }
                    });
                }else{
                    System.out.println("连接失败");
                }
            });
            RpcResponse rpcResponse = rpcResponseCompletableFuture.get();
            //关闭连接
            netClient.close();
            return rpcResponse.getData();*/

//        try {
//            // 序列化请求体
//            byte[] bodyBytes = serializer.serialize(rpcRequest);
//
//            // 获取全局 RPC 配置
//            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
//            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
//
//            // 通过注册中心类型（如 "zookeeper,etcd"）获取 Registry 实例
//            //单例
//            Registry registry = RpcApplication.getRegistry();
//
//            // 构造服务元信息用于服务发现
//            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
//            serviceMetaInfo.setServiceName(serviceName);
//            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
//            System.out.println("serviceMetaInfo"+serviceMetaInfo);
//
//            // 从注册中心发现可用的服务实例列表
//            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo);
//            //如果所有节点为空，抛出异常
//            if (CollUtil.isEmpty(serviceMetaInfoList)) {
//                throw new RuntimeException("暂无可用的服务地址: " + serviceName);
//            }
//
//            // 暂时选择第一个服务实例（后续可替换为负载均衡策略）
////            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
//            // 负载均衡
//            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
//            // 将调用方法名（请求路径）作为负载均衡参数
//            Map<String, Object> requestParams = new HashMap<>();
//            requestParams.put("methodName", rpcRequest.getMethodName());
//            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
//            /*// 发送 HTTP 请求并处理响应
//            // 根据注册中心中的服务端的信息向服务端发送请求
//            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
//                    .body(bodyBytes)
//                    .execute()) {
//                //获取到请求
//                byte[] result = httpResponse.bodyBytes();
//                // 反序列化响应
//                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
//                return rpcResponse.getData();
//            }*/
//
//            //封装
//            //return VertxTcpClient.doRequest(rpcRequest,selectedServiceMetaInfo).getData();
//            RpcResponse rpcResponse = null;
//            try{
//
//                /**
//                 * 这里直接对TCP进行封装
//                 * 重试策略
//                 */
//                rpcResponse = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy()).doRetry(() -> {
//                    return VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
//                });
//            }catch (Exception e){
//                /**
//                 * 容错策略
//                 */
//                TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
//                rpcResponse = tolerantStrategy.doTolerant(null, e);
//            }
//            return rpcResponse.getData();
//          /*  //使用TCP
//            Vertx vertx = Vertx.vertx();
//            NetClient netClient = vertx.createNetClient();
//            CompletableFuture<RpcResponse> rpcResponseCompletableFuture = new CompletableFuture<>();
//            netClient.connect(selectedServiceMetaInfo.getServicePort(),selectedServiceMetaInfo.getServiceHost(),result->{
//                //如果发送成功
//                if (result.succeeded()) {
//                    System.out.println("Connected to TCP server");
//                    NetSocket socket = result.result();
//                    //发送消息
//                    //构造消息
//                    ProtocolMessage<RpcRequest> rpcResponseProtocolMessage = new ProtocolMessage<>();
//                    ProtocolMessage.Header header = new ProtocolMessage.Header();
//                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
//                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
//                    //通过配置文件进行指定序列化
//                    header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
//                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
//                    header.setRequestId(IdUtil.getSnowflakeNextId());
//
//                    rpcResponseProtocolMessage.setHeader(header);
//                    rpcResponseProtocolMessage.setBody(rpcRequest);
//                    //编码请求
//                    try{
//                        Buffer encodeBuffer = ProtocolMessageEncoder.encode(rpcResponseProtocolMessage);
//                        socket.write(encodeBuffer);
//                    } catch (IOException e) {
//                        throw new RuntimeException("协议消息编码失败",e);
//                    }
//
//                    //接受响应
//                    socket.handler(buffer -> {
//                        try {
//                            ProtocolMessage<RpcResponse> rpcResponseDecode = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
//                            rpcResponseCompletableFuture.complete(rpcResponseDecode.getBody());
//                        } catch (IOException e) {
//                            throw new RuntimeException("协议消息解码失败",e);
//                        }
//                    });
//                }else{
//                    System.out.println("连接失败");
//                }
//            });
//            RpcResponse rpcResponse = rpcResponseCompletableFuture.get();
//            //关闭连接
//            netClient.close();
//            return rpcResponse.getData();*/
//        }catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
}