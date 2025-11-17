package org.example.config;

import lombok.Data;
import org.example.serializer.SerializerKeys;

@Data
public class RpcConfig {
    /**
     * 名称
     * */
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
    private String serializer = SerializerKeys.JDK;

    /**
     *
     注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
}
