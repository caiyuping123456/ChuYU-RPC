package org.example.rpcspringbootprovider.service;

import org.example.model.User;
import org.example.rpc.springbootstart.annotation.RpcService;
import org.example.service.UserService;
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
