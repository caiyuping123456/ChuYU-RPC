package org.example.springrpcprovider.service;

import org.example.model.User;
import org.example.service.UserService;
import org.example.springboot.rpc.core.annotation.RpcService;
import org.springframework.stereotype.Service;

@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名：" + user.getName());
        return user;
    }
}

