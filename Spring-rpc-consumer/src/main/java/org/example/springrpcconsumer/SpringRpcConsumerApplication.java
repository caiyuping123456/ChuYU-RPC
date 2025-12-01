package org.example.springrpcconsumer;

import org.example.springboot.rpc.core.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableRpc(needServer = false)
@ComponentScan(basePackages = {
    "org.example.springboot.rpc.core",
})
public class SpringRpcConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRpcConsumerApplication.class, args);
    }

}
