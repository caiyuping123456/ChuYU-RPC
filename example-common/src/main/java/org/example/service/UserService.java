package org.example.service;

import org.example.model.User;

public interface UserService {
    User getUser(User user);

    //获取默认的short值
    default short getNum(){
        return 1;
    }
}
