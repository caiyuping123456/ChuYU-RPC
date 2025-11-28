package org.example.rpcspringbootprovider;

import org.example.rpc.springbootstart.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@EnableRpc
@SpringBootApplication
public class RpcSpringBootProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcSpringBootProviderApplication.class, args);
    }

}
