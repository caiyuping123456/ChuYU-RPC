package org.example.springboot.rpc.core.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.example.springboot.rpc.core.config.RpcApplication;
import org.example.springboot.rpc.core.config.RpcConfig;
import org.example.springboot.rpc.core.constant.RpcConstant;
import org.example.springboot.rpc.core.http.tcp.VertxTcpClient;
import org.example.springboot.rpc.core.loadbalancer.LoadBalancer;
import org.example.springboot.rpc.core.loadbalancer.LoadBalancerManager;
import org.example.springboot.rpc.core.model.RpcRequest;
import org.example.springboot.rpc.core.model.RpcResponse;
import org.example.springboot.rpc.core.model.ServiceMetaInfo;
import org.example.springboot.rpc.core.registry.Registry;
import org.example.springboot.rpc.core.registry.RegistryManager;
import org.example.springboot.rpc.core.serializer.Serializer;
import org.example.springboot.rpc.core.serializer.SerializerManager;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
        Serializer serializer = SerializerManager.getSerializer(RpcApplication.getRpcConfig().getSerializer());
        String serviceName = method.getDeclaringClass().getName();
        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            //application
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();

            // 通过注册中心类型（如 "zookeeper,etcd"）获取 Registry 实例(单例)
            String RegistryKey = rpcConfig.getRegistryConfig().getRegistry();
            Registry registry = RegistryManager.getRegistry(RegistryKey);

            // 构造服务元信息用于服务发现
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);

            // 从注册中心发现可用的服务实例列表
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo);
            //如果所有节点为空，抛出异常
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无可用的服务地址: " + serviceName);
            }
            // 负载均衡策略
            LoadBalancer loadBalancer = LoadBalancerManager.getLoadBalancer(rpcConfig.getLoadBalancer());
            // 将调用方法名（请求路径）作为负载均衡参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName", rpcRequest.getMethodName());
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);


            return VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo).getData();
            // 发送请求
//            try (HttpResponse httpResponse = HttpRequest.post(getUrl(rpcConfig.getServerHost(),rpcConfig.getServerPort()))
//                    .body(bodyBytes)
//                    .execute()) {
//                byte[] result = httpResponse.bodyBytes();
//                // 反序列化
//                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
//                return rpcResponse.getData();
//            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
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