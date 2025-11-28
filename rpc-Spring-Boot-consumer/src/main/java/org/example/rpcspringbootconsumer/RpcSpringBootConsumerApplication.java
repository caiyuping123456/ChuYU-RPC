package org.example.rpcspringbootconsumer;

import org.example.rpc.springbootstart.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRpc(needServer = false)
public class RpcSpringBootConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcSpringBootConsumerApplication.class, args);
    }

}
