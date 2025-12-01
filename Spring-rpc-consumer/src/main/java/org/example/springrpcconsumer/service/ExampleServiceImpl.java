package org.example.springrpcconsumer.service;

import org.example.model.User;
import org.example.service.UserService;
import org.example.springboot.rpc.core.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {
    @RpcReference
    private UserService userService;
    public void test() {
        User user = new User();
        user.setName("chuyu");
        User resultUser = userService.getUser(user);
        if (resultUser != null) {
            System.out.println(resultUser.getName());
        }else {
            System.out.println("mock测试通过");
        }

    }
}