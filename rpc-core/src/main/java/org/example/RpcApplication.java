package org.example;


import lombok.extern.slf4j.Slf4j;
import org.example.config.RegistryConfig;
import org.example.config.RpcConfig;
import org.example.constant.RpcConstant;
import org.example.registry.Registry;
import org.example.registry.RegistryFactory;
import org.example.utils.ConfigUtils;


/**
 * RPC 框架应用
 * 相当于 holder，存放了项目全局用到的变量。双检锁单例模式实现
 */
@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;
    private static volatile Registry registry;

    /**
     * 可以自己传入配置
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig){
        rpcConfig = newRpcConfig;
        log.info("rpc init, config = {}", newRpcConfig.toString());
        //注册配置中心
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("Registry init {}", registryConfig);

        //注册ShutDownHook操作，JVM退出时自动执行
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    /**
     * 初始化
     * */
    public static void init(){
        RpcConfig newRpcConfig;
        try{
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        }catch (Exception e){
            //失败，使用默认配置
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     * */
    public static RpcConfig getRpcConfig(){
        if (rpcConfig==null) {
            synchronized (RpcApplication.class){
                if(rpcConfig==null){
                    init();
                }
            }
        }
        return rpcConfig;
    }

    public static Registry getRegistry(){
        if (registry==null) {
           throw new RuntimeException("registry为空");
        }
        return registry;
    }
}

