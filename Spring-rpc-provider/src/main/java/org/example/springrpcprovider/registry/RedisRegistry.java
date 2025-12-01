package org.example.springrpcprovider.registry;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.rpc.core.annotation.RpcRegistry;
import org.example.springboot.rpc.core.config.RegistryConfig;
import org.example.springboot.rpc.core.constant.RpcConstant;
import org.example.springboot.rpc.core.model.ServiceMetaInfo;
import org.example.springboot.rpc.core.registry.Registry;
import org.example.springboot.rpc.core.registry.RegistryServiceCache;
import org.springframework.stereotype.Component;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Redis的注册中心
 */
@Slf4j
//@RpcRegistry("redis")
//@Component
public class RedisRegistry implements Registry {

    private JedisPooled jedisPooled;

    //节点key信息
    final private Set<String> localRegistryInfo = new HashSet<>();

    /**
     * 根节点
     */
    private static final String REDIS_ROOT_PATH = "/rpc/";
    /**
     * 服务信息缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    private volatile Boolean cronStarted = false;

    @Override
    public void init(RegistryConfig registryConfig) {
        JedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                .password(registryConfig.getPassword())
                .timeoutMillis(Math.toIntExact(registryConfig.getTimeout()))
               .build();
        //创建连接对象
        jedisPooled = new JedisPooled(new HostAndPort("localhost",6379),jedisClientConfig);

        //开启定时任务
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        //key值
        String key = REDIS_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        //将信息转为json
        String jsonStr = JSONUtil.toJsonStr(serviceMetaInfo);
        //存入同时设置过期时间
        long expireSeconds = RpcConstant.MAX_TIMEOUT.getSeconds();
        jedisPooled.set(key, jsonStr);
        jedisPooled.expire(key,expireSeconds);
        //将节点key添加到本地的信息中
        localRegistryInfo.add(key);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        //通过jedis进行删除
        String key = REDIS_ROOT_PATH + serviceMetaInfo;
        jedisPooled.del(key);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(ServiceMetaInfo serviceMetaInfo) {
        //从缓存中获取
        List<ServiceMetaInfo> serviceMetaInfos1 = registryServiceCache.readCache();
        if(serviceMetaInfos1!=null && serviceMetaInfos1.size()!=0){
            return serviceMetaInfos1;
        }
        String key = REDIS_ROOT_PATH + serviceMetaInfo.getServiceKey()+"/";
        ArrayList<ServiceMetaInfo> serviceMetaInfos = new ArrayList<>();
        //使用scan
        String scanKey = "0";
        try{
            do {
                ScanParams scanParams = new ScanParams().match(key + "*");
                ScanResult<String> scan = jedisPooled.scan(scanKey, scanParams);
                List<String> result = scan.getResult();
                if (!result.isEmpty()) {
                    // 批量获取 key 对应的 value
                    List<String> values = jedisPooled.mget(result.toArray(new String[0]));
                    for (String value : values) {
                        if (value != null) {
                            ServiceMetaInfo instance = JSONUtil.toBean(value, ServiceMetaInfo.class);
                            serviceMetaInfos.add(instance);
                        }
                    }
                }
                scanKey = scan.getCursor();
            }while(!"0".equals(scanKey));
        }catch (Exception e){
            throw new RuntimeException("服务发现失败", e);
        }
        //存入缓存
        registryServiceCache.writeCache(serviceMetaInfos);
        return serviceMetaInfos;
    }

    /**
     * 监听（服务端）
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {

    }

    @Override
    public void destroy() {
        //当前节点下线（就是服务端下线）
        //只要将本地信息中的记录连同etcd中的记录删除就可以
        log.info("当前节点下线");
        for(String key : localRegistryInfo){
            jedisPooled.del(key);
        }
        //清除本地
        localRegistryInfo.clear();
        if (jedisPooled != null){
            jedisPooled.close();
        }
    }

    @Override
    public void heartBeat() {
        //开启秒级精度
        CronUtil.setMatchSecond(true);

        CronUtil.schedule("*/10 * * * * *", (Task) ()->{
            log.info("定时任务执行了");
            //遍历节点获取节点信息
            for (String key : localRegistryInfo){
                String value = jedisPooled.get(key);
                //过期，剔除
                if (StringUtil.isNullOrEmpty(value)) continue;
                //未过期，续约
                try {
                    long expireSeconds = RpcConstant.MAX_TIMEOUT.getSeconds();
                    jedisPooled.set(key, value);
                    jedisPooled.expire(key, expireSeconds);
                }catch (Exception e){
                    throw new RuntimeException(key+"续约失败",e);
                }

            }
        });
        //启动
        CronUtil.start();
    }
}


