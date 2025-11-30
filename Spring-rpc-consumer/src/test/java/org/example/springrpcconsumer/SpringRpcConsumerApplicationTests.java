package org.example.springrpcconsumer;

import org.example.model.User;
import org.example.service.UserService;
import org.example.springboot.rpc.core.config.RpcConfig;
import org.example.springboot.rpc.core.proxy.ServiceProxyFactory;
import org.example.springrpcconsumer.service.ExampleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SpringRpcConsumerApplicationTests {

    @Resource
    private ExampleServiceImpl exampleService;

    @Test
    void test01(){
        exampleService.test();
    }

}
