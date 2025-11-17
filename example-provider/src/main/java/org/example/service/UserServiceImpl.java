package org.example.service;

import org.example.model.User;

public class UserServiceImpl implements UserService{
    @Override
    public User getUser(User user) {
        System.out.println("用户名字："+user.getName());
        return user;
    }
}
