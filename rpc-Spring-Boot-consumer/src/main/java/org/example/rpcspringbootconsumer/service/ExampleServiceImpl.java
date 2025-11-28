package org.example.rpcspringbootconsumer.service;

import org.example.model.User;
import org.example.rpc.springbootstart.annotation.RpcReference;
import org.example.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {
    @RpcReference
    private UserService userService;
    public void test() {
        User user = new User();
        user.setName("chuyu");
        User resultUser = userService.getUser(user);
        System.out.println(resultUser.getName());
    }
}