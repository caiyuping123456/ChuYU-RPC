package org.example;

import org.example.loadbalancer.ConsistentHashLoadBalancer;
import org.example.loadbalancer.LoadBalancer;
import org.example.loadbalancer.RandomLoadBalancer;
import org.example.loadbalancer.RoundRobinLoadBalancer;
import org.example.model.ServiceMetaInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ���ؾ������
 */
public class LoadBalancerTest {

    /**
     * ���
     */
    // final LoadBalancer loadBalancer = new RandomLoadBalancer();

    /**
     * ��ѯ
     */
    // final LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    /**
     * һ���Թ�ϣ
     */
    final LoadBalancer loadBalancer = new ConsistentHashLoadBalancer();
    @Test
    public void select() {
        // �������
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", "1245523hahahasddsdsd");
        // �����б�
        ServiceMetaInfo serviceMetaInfo1 = new ServiceMetaInfo();
        serviceMetaInfo1.setServiceName("myService");
        serviceMetaInfo1.setServiceVersion("1.0");
        serviceMetaInfo1.setServiceHost("localhost");
        serviceMetaInfo1.setServicePort(1234);
        ServiceMetaInfo serviceMetaInfo2 = new ServiceMetaInfo();
        serviceMetaInfo2.setServiceName("myService");
        serviceMetaInfo2.setServiceVersion("1.0");
        serviceMetaInfo2.setServiceHost("yupi.icu");
        serviceMetaInfo2.setServicePort(80);
        List<ServiceMetaInfo> serviceMetaInfoList = Arrays.asList(serviceMetaInfo1,serviceMetaInfo2);
        // �������� 3 ��
        ServiceMetaInfo serviceMetaInfo = loadBalancer.select(requestParams,serviceMetaInfoList);
        System.out.println(serviceMetaInfo);
        Assert.assertNotNull(serviceMetaInfo);
        serviceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println(serviceMetaInfo);
        Assert.assertNotNull(serviceMetaInfo);
        serviceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println(serviceMetaInfo);
        Assert.assertNotNull(serviceMetaInfo);
    }

}
