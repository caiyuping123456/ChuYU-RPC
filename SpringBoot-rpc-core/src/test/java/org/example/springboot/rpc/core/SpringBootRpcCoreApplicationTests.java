package org.example.springboot.rpc.core;

import org.example.springboot.rpc.core.config.RpcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SpringBootRpcCoreApplicationTests {

    @Resource
    private RpcConfig rpcConfig;

    @Test
    void TestConfig() {
        System.out.println(rpcConfig);
    }

}
