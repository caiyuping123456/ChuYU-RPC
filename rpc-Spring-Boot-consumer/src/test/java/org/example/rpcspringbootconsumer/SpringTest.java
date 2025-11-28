package org.example.rpcspringbootconsumer;

import org.example.rpcspringbootconsumer.service.ExampleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class SpringTest {
    @Resource
    private ExampleServiceImpl exampleService;

    @Test
    void test01(){
        exampleService.test();
    }
}
