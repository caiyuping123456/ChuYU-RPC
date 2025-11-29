package org.example.springboot.rpc.core.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


/**
 * RPC 框架应用
 * 相当于 holder，存放了项目全局用到的变量。双检锁单例模式实现
 */
@Slf4j
@Configuration
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    @Autowired
    public void setRpcConfig(RpcConfig rpcConfig) {
        RpcApplication.rpcConfig = rpcConfig;
    }

    /**
     * 获取配置
     * */
    public static RpcConfig getRpcConfig(){
        return rpcConfig;
    }

}

