package org.example.springrpcprovider;

import org.example.springboot.rpc.core.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableRpc
@ComponentScan(basePackages = {
    "org.example.springboot.rpc.core",
})
public class SpringRpcProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRpcProviderApplication.class, args);
    }

}
