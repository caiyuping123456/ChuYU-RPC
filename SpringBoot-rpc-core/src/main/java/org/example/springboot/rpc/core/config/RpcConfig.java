package org.example.springboot.rpc.core.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
public class RpcConfig {

    /**
     * 名称
     * *
    */
    private String name="chuyuRpc";

    /**
     * 版本号
     * */
    private String version="1.0";

    /**
     * 服务ip
     * */
    private String serverHost="localhost";

    /**
     * 服务端口
     * */
    private Integer serverPort=8080;

    /**
     * 固定值设置
     * */
    private boolean mock = false;

    /**
     * 序列化器（默认为json）
     */
    private String serializer = "json";

    /**
     *
     注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();

    /**
     * 负载均衡器(默认为随机)
     */
    private String loadBalancer = "random";

    /**
     * 重试策略(no重试机制)
     */
    private String retryStrategy = "no";

    /**
     * 容错策略(默认是快速失效策略)
     */
    private String tolerantStrategy = "";
}
