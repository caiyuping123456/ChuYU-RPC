package org.example.loadbalancer;

import org.example.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机负载均衡器
 */
public class RandomLoadBalancer implements LoadBalancer{

    //随机数
    private final Random random = new Random();
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        //无服务，直接返回
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }
        int size = serviceMetaInfoList.size();
        //只有一个，直接返回
        if (size==1) return serviceMetaInfoList.get(0);
        //在服务数之间进行随机选取
        return serviceMetaInfoList.get(random.nextInt(size));
    }
}
