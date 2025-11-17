package org.example.registry;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.vertx.core.impl.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import org.example.config.RegistryConfig;
import org.example.constant.RpcConstant;
import org.example.model.ServiceMetaInfo;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 这个是ETCD的注册中心
 */
@Slf4j
public class EtcdRegistry implements Registry{

    /**
     * 主客户端对象，用于通信
     */
    private Client client;
    /**
     * 键值对操作对象，用于操作
     */
    private KV kvClient;
    /**
     * 本机注册对象key节点，用于续费
     */
    final private Set<String> localRegisterNodeKeySet = new HashSet<>();
    /**
     * 注册中心服务缓存
     */
    final private RegistryServiceCache registryServiceCache = new RegistryServiceCache();
    /**
     * 正在监听的key集合
     */
    private final Set<String> watchKeySet = new ConcurrentHashSet<>();

    /**
     * 根节点
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";
    @Override
    public void init(RegistryConfig registryConfig) {
        //通过Client进行etcd的连接，同时设置了超时时间（30秒）
        client = Client.builder().endpoints(registryConfig.getAddress()).connectTimeout(RpcConstant.MAX_TIMEOUT).build();
        //通过Client获取操作对象，通过这个可以对etcd里面的键值进行操作。
        kvClient = client.getKVClient();

        //开启定时任务
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建 Lease 和 KV 客户端
        Lease leaseClient = client.getLeaseClient();
        // 创建一个 30 秒的租约
        //租约可以理解为30秒内一直保持连接，达到30秒后etcd会自动进行删除
        long leaseId = leaseClient.grant(30).get().getID();
        // 设置要存储的键值对
        //这个是生成一个key
        //类似于redis的，保证了在同一个目录，同时同一个目录下服务的key是服务名字加上服务版本号及服务的全部信息。
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        //etcd的底层是根据gRPC实现的，要保证是ByteSequence，所以这里是将key和value进行转为这个类型
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        //注意注册中心只是把服务端的信息保存
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);
        // 将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        //将服务信息写入注册中心
        kvClient.put(key, value, putOption).get();

        //将节点key信息添加到本地中
        localRegisterNodeKeySet.add(registerKey);
        log.info("注册服务 key: {}", registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registryKey = ETCD_ROOT_PATH+serviceMetaInfo.getServiceNodeKey();
        //根据key值删除注册的信息
        kvClient.delete(ByteSequence.from(registryKey,StandardCharsets.UTF_8));
        //同时删除本地的节点key信息
        localRegisterNodeKeySet.remove(registryKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(ServiceMetaInfo serviceMetaInfo) {
        log.info("serviceMetaInfo",serviceMetaInfo);
        //优先从缓存中获取
        List<ServiceMetaInfo> serviceMetaInfos = registryServiceCache.readCache();
        if(serviceMetaInfos!=null && serviceMetaInfos.size()!=0){
            return serviceMetaInfos;
        }
        // 构造前缀：/rpc/服务名/
        String searchPrefix = ETCD_ROOT_PATH + serviceMetaInfo.getServiceKey() + "/";

        try {
            // 构建前缀查询选项
            GetOption getOption = GetOption.builder().isPrefix(true).build();

            // 执行 etcd 前缀查询（带超时）
            List<KeyValue> keyValues = kvClient.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption
                    )
                    .get(RpcConstant.TIME_OUT, TimeUnit.SECONDS)  // 设置 3 秒超时
                    .getKvs();

            // 解析并转换为 ServiceMetaInfo 列表
            return keyValues.stream()
                    .map(keyValue -> {
                        ByteSequence valueBytes = keyValue.getValue();
                        if (valueBytes == null || valueBytes.size() == 0) {
                            return null; // 跳过无效数据
                        }
                        // 监听 key 的变化（在获取value之前监听）
                        watch(keyValue.getKey().toString());
                        //存储的是JSON数据，所以取出的也是JSON数据
                        String json = valueBytes.toString(StandardCharsets.UTF_8);
                        try {
                            return JSONUtil.toBean(json, ServiceMetaInfo.class);
                        } catch (Exception ex) {
                            // 可记录日志，但不中断整个流程
                            log.warn("Failed to parse service metadata: {}", json, ex);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull) // 过滤掉解析失败的项
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败: " + serviceMetaInfo.getServiceKey(), e);
        }
    }

    /**
     * 监听（服务端）
     * 用于保证提供端下线后可以同步清理缓存
     * 避免消费者消费旧数据
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        //获取监听器
        Watch watchClient = client.getWatchClient();
        //这里是通过map集合判断（如果里面没有，表示一以前没有监听过）
        boolean newWatch = watchKeySet.add(serviceNodeKey);
        //为true表示需要监听
        if(newWatch){
            watchClient.watch(
                    ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8),
                    response -> {
                        for (WatchEvent event : response.getEvents()) {
                            switch (event.getEventType()) {
                                // key被删除时触发
                                case DELETE:
                                    //删除缓存
                                    registryServiceCache.clearCache();
                                    break;
                                case PUT:
                                default:
                                    break;
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void destroy() {
        //销毁连接对象
        //当前节点下线（就是服务端下线）
        //只要将本地信息中的记录连同etcd中的记录删除就可以
        log.info("当铺节点下线");
        for(String key : localRegisterNodeKeySet){
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key+"节点下线失败",e);
            }
        }
        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void heartBeat() {
        //设计秒级精度
        CronUtil.setMatchSecond(true);
        //10进行一次续约
        CronUtil.schedule("*/20 * * * * *", (Task) () -> {
            //遍历本地节点key
            for(String key : localRegisterNodeKeySet){
                try {
                    //获取当前key信息的所有节点信息
                    List<KeyValue> kvs = kvClient.get(ByteSequence.from(key,StandardCharsets.UTF_8))
                            .get().getKvs();
                    //判断当前节点是否过期
                    //过期，跳过
                    if (CollUtil.isEmpty(kvs)) {
                        continue;
                    }

                    //未过期，进行再续约（就是进行再注册）
                    KeyValue keyValue = kvs.get(0);
                    //获取节点value
                    //取出的是json字符串
                    String json = keyValue.getValue().toString(StandardCharsets.UTF_8);
                    //反序列化
                    ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(json, ServiceMetaInfo.class);
                    register(serviceMetaInfo);
                } catch (Exception e) {
                    throw new RuntimeException(key+"续约失败",e);
                }
            }
        });
        //启动
        CronUtil.start();
    }
}
