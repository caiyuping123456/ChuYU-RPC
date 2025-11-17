package org.example.registry;

import org.example.config.RegistryConfig;
import org.example.model.ServiceMetaInfo;

import java.util.List;

//TODO
public class ZookeeperRegistry implements Registry{
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
