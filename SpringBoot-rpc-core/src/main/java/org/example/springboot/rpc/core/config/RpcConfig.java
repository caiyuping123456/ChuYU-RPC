package org.example.springboot.rpc.core.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "rpc")
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
     * 序列化器
     */
    private String serializer = "";

    /**
     *
     注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();

    /**
     * 负载均衡器
     */
    private String loadBalancer = "";

    /**
     * 重试策略
     */
    private String retryStrategy = "";

    /**
     * 容错策略(默认是快速失效策略)
     */
    private String tolerantStrategy = "";
}
