package org.example.springrpcprovider.config;

import org.example.springboot.rpc.core.config.RpcApplication;
import org.example.springboot.rpc.core.config.RpcConfig;
import org.example.springboot.rpc.core.http.HttpService;
import org.example.springboot.rpc.core.http.VertxHttpServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreAutoConfig {

    @Bean
    public HttpService httpService() {
        return new VertxHttpServer();
    }
    @Bean
    public RpcApplication rpcApplication(){
        return new RpcApplication();
    }
    @Bean
    public RpcConfig rpcConfig(){
        return new RpcConfig();
    }
}