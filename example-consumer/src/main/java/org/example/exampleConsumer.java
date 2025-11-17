package org.example;

import org.example.config.RpcConfig;
import org.example.model.User;
import org.example.proxy.ServiceProxyFactory;
import org.example.service.UserService;
import org.example.utils.ConfigUtils;

/**
 * Hello world!
 *
 */
public class exampleConsumer {
    public static void main( String[] args ) {
        //获取配置文件
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);

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
//        short num = userService.getNum();
//        System.out.println(num);
    }
}
