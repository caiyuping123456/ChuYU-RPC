package org.example.springrpcprovider;

import org.example.service.UserService;
import org.example.springboot.rpc.core.config.RpcApplication;
import org.example.springboot.rpc.core.http.HttpService;
import org.example.springboot.rpc.core.http.VertxHttpServer;
import org.example.springboot.rpc.core.registry.LocalRegistry;
import org.example.springrpcprovider.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SpringRpcProviderApplicationTests {

    @Resource
    private HttpService httpService;
    @Resource
    private RpcApplication rpcApplication;

    @Test
    void contextLoads() {
        //初始化;
        System.out.println("配置文件"+RpcApplication.getRpcConfig());
        //注册服务（注册到本地）
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 启动 web 服务
        httpService.doStart(RpcApplication.getRpcConfig().getServerPort());
    }

}
