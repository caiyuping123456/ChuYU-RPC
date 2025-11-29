package org.example.springrpcprovider.service;

import org.example.model.User;
import org.example.service.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名字："+user.getName());
        return user;
    }
}
