package org.example;

import org.example.config.RegistryConfig;
import org.example.config.RpcConfig;
import org.example.http.HttpService;
import org.example.http.VertxHttpServer;
import org.example.http.tcp.VertxTcpServer;
import org.example.model.ServiceMetaInfo;
import org.example.registry.LocalRegistry;
import org.example.registry.Registry;
import org.example.registry.RegistryFactory;
import org.example.service.UserService;
import org.example.service.UserServiceImpl;

/**
 * Hello world!
 *
 */
public class EasyProviderExample {
    public static void main( String[] args ) {
        //初始化
        RpcApplication.init();

        //注册服务（注册到本地）
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        //注册到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        //获取到注册中心的配置
        Registry registry = RpcApplication.getRegistry();
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();

        //这个是接口名字
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            //进行注册
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //启动服务器
/*        HttpService vertxHttpServer = new VertxHttpServer();
        vertxHttpServer.doStart(RpcApplication.getRpcConfig().getServerPort());*/

        // 启动 TCP 服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
