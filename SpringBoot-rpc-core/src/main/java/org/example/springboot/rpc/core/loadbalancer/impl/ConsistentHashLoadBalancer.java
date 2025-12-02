package org.example.springboot.rpc.core.loadbalancer.impl;

import org.example.springboot.rpc.core.annotation.RpcLoadBalancer;
import org.example.springboot.rpc.core.loadbalancer.LoadBalancer;
import org.example.springboot.rpc.core.model.ServiceMetaInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性哈希负载均衡器
 */
@RpcLoadBalancer(loadBalancer = "consistentHash")
@Component
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一致性Hash环
    */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    /**
     * 虚拟节点
     */
    private final Integer VIRTUAL_NODE_NUM = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }
        //构建虚拟哈希环
        for(ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList){
            for(int i = 0 ; i < VIRTUAL_NODE_NUM ; i++){
                int hash = this.getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNodes.put(hash,serviceMetaInfo);
            }
        }

        //获取请求的hash值
        int hash = this.getHash(requestParams);
        // 顺时针查找第一个不小于请求哈希值的虚拟节点。
        Map.Entry<Integer, ServiceMetaInfo> integerServiceMetaInfoEntry = virtualNodes.ceilingEntry(hash);
        if (integerServiceMetaInfoEntry == null) {
            return virtualNodes.firstEntry().getValue();
        }
        return integerServiceMetaInfoEntry.getValue();
    }

    /**
     * 哈希算法
     * @param key
     * @return
     */
    private int getHash(Object key) {
        return key.hashCode();
    }
}
