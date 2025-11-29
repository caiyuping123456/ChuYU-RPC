package org.example.springrpcconsumer.service;

import org.example.model.User;
import org.example.service.UserService;

public class UserServiceMack implements UserService {

    @Override
    public User getUser(User user){
        return null;
    }

    //获取默认的short值
    @Override
    public short getNum(){
        return 1;
    }
}
