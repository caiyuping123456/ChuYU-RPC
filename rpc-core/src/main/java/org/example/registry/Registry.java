package org.example.registry;

import org.example.config.RegistryConfig;
import org.example.model.ServiceMetaInfo;

import java.util.List;

/**
 * 这个是注册中心接口，所有的注册中心都要实现这个接口，如这里使用的ETCD
 * 或者是redis，zookerp等
 */
public interface Registry {

    /**
     * 初始化
     * @Parameter registryConfig
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务（服务端）
     * @Parameter serviceMetaInfo
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 注销服务（服务端）
     * @Parameter serviceMetaInfo
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo);

    /**
     * 服务发现（获取某服务的所有节点，消费端）
     * @Parameter serviceKey (服务键名)
     * @Return
     */
    List<ServiceMetaInfo> serviceDiscovery(ServiceMetaInfo serviceMetaInfo);

    /**
     * 监听（消费端）
     */
    void watch(String serviceNodeKey);

    /**
     * 服务销毁
     */
    void destroy();

    /**
     * 心跳检测
     */
    void heartBeat();

}
