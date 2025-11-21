package org.example.loadbalancer;

import org.example.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡器（消费端使用）
 * 用于从多个可用的服务实例中，根据某种策略选择一个最合适的服务节点进行调用。
*/
public interface LoadBalancer {

    /**
     * 选择服务调用
     *
     * @param requestParams       当前请求的参数（可用于实现基于请求内容的智能路由，如灰度发布、参数路由等）
     * @param serviceMetaInfoList 可用的服务元信息列表（即服务提供者的地址、端口、权重等信息）
     * @return 选中的服务元信息；若无可选服务，应抛出异常或返回 null（建议抛出异常）
     * @throws IllegalArgumentException 当服务列表为空或无效时
     */
    ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}