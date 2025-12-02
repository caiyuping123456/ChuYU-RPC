package org.example.springboot.rpc.core.loadbalancer.impl;

import org.example.springboot.rpc.core.annotation.RpcLoadBalancer;
import org.example.springboot.rpc.core.loadbalancer.LoadBalancer;
import org.example.springboot.rpc.core.model.ServiceMetaInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器
 */
@RpcLoadBalancer(loadBalancer = "roundRobin")
@Component
public class RoundRobinLoadBalancer implements LoadBalancer {

    /**
     * 当前轮询的下标
     * 这个是JUC的原子计数器
     * AtomicInteger 是 Java 并发包（java.util.concurrent.atomic）提供的线程安全整数类。
     * 它通过 CAS（Compare-And-Swap） 机制保证对整数的增减操作是原子的，无需加锁。
     * currentIndex 记录下一次应该选择的服务在列表中的索引位置。
     * 初始值为 0，表示第一次调用时选第 0 个服务。
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        // 没有服务元数据，直接返回null
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }
        int size = serviceMetaInfoList.size();
        // 一个服务元数据，直接返回
        if (size==1) {
            return serviceMetaInfoList.get(0);
        }
        //不是一个，进行取模
        /**
         * 原子地执行：先返回当前值，再自增 1。
         * 例如：
         * 初始值：0 → 返回 0，然后变成 1；
         * 下次调用：返回 1，变成 2；
         * …
         * 到达 size-1 后，下一次返回 size，但 % size 后变为 0，实现循环轮询
         */
        int index = currentIndex.getAndIncrement() % size;
        /**
         * 服务列表有 3 个实例（A、B、C）
         *
         * 调用顺序：
         *
         * currentIndex=0 → 0 % 3 = 0 → 返回 A
         * currentIndex=1 → 1 % 3 = 1 → 返回 B
         * currentIndex=2 → 2 % 3 = 2 → 返回 C
         * currentIndex=3 → 3 % 3 = 0 → 返回 A
         * …… 循环往复
         */
        return serviceMetaInfoList.get(index);
    }
}
