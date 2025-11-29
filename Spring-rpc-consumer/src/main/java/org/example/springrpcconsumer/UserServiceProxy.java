package org.example.springrpcconsumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.example.model.User;
import org.example.service.UserService;

public class UserServiceProxy implements UserService {
    public User getUser(User user) {
       return null;
    }
}
