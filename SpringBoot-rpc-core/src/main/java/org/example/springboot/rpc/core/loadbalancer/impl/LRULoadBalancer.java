package org.example.springboot.rpc.core.loadbalancer.impl;

import org.example.springboot.rpc.core.loadbalancer.LoadBalancer;
import org.example.springboot.rpc.core.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 最少活跃数负载均衡器，选择当前正在处理请求的数量最少的服务提供者
 */
// TODO
public class LRULoadBalancer implements LoadBalancer {
    /**
     * 选择器，用于存储当前的服务节点和对应的次数
     */
    final static Map<ServiceMetaInfo,Integer> LRUMap = new ConcurrentHashMap<>();
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        ServiceMetaInfo ans = null;
        Integer cnt = 0;
        //第一步，通过比对,没有的直接返回
        for(ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList){
            if(!LRUMap.containsKey(serviceMetaInfo)) return serviceMetaInfo;
            Integer comparato = LRUMap.get(serviceMetaInfo);
            if(comparato.compareTo(cnt) > 0){
                cnt = comparato;
                ans = serviceMetaInfo;
            }
        }
        LRUMap.put(ans,cnt++);
        return ans;
    }

}
