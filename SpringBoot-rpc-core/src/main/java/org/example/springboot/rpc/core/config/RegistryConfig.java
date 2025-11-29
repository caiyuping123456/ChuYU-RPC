package org.example.springboot.rpc.core.config;

import lombok.Data;

/**
 * 这个是用于连接注册中心的配置类
 */
@Data
public class RegistryConfig {

    /**
     * 注册中心类别(默认)
     */
    private String registry = "etcd";

    /**
     * 注册中心地址
     */
    private String address = "http://localhost:2380";

    /**
     * 用户名
     */
    private String username="admin";

    /**
     * 密码
     */
    private String password = "123456";

    /**
     * 超时时间（单位毫秒）
     */
    private Long timeout = 10000L;
}
