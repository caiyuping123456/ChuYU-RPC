package org.example.springrpcconsumer;

import org.example.model.User;
import org.example.service.UserService;
import org.example.springboot.rpc.core.config.RpcConfig;
import org.example.springboot.rpc.core.proxy.ServiceProxyFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SpringRpcConsumerApplicationTests {

    @Resource
    private RpcConfig rpcConfig;

    @Test
    void Test001() {
        //获取配置文件
        System.out.println(rpcConfig);

        //通过代理对象发送请求
        UserService userService =   ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("小明");
        //调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println("通过rpm获取的名字："+newUser.getName());
        }else{
            System.out.println("等于空");
        }
    }

}
