package org.example.springboot.rpc.core.registry.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.RpcRegistry;
import org.example.springboot.rpc.core.config.RegistryConfig;
import org.example.springboot.rpc.core.model.ServiceMetaInfo;
import org.example.springboot.rpc.core.registry.Registry;
import org.springframework.stereotype.Component;

import java.util.List;

//TODO
@Slf4j
@RpcRegistry("zookeeper")
@Component
public class ZookeeperRegistry implements Registry {
    @Override
    public void init(RegistryConfig registryConfig) {

    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {

    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {

    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(ServiceMetaInfo serviceMetaInfo) {
        return null;
    }

    @Override
    public void watch(String serviceNodeKey) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void heartBeat() {

    }
}
