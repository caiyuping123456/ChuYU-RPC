package org.example.springboot.rpc.core.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;


/**
 * RPC 框架应用
 * 相当于 holder，存放了项目全局用到的变量。双检锁单例模式实现
 */
@Slf4j
public class RpcApplication {
    // 保持 volatile 确保多线程可见性
    private static volatile RpcConfig rpcConfig;

    /**
     * 【新的初始化入口】
     * 依赖 CustomConfigRunListener 在极早期加载的配置。
     */
    public static void init() {
        // 只有当 rpcConfig 为空时才进行初始化，实现双重检查锁的线程安全单例效果
        if (rpcConfig == null) {
            // 假设 CustomConfigRunListener 已经被正确注册和执行
            final RpcConfig loadedConfig = CustomConfigRunListener.getGlobalRpcConfig();
            // 确保线程安全地设置
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    rpcConfig = loadedConfig;
                    log.info("[RPC App] RpcConfig 已通过静态初始化注入。");
                }
            }
        }
    }

    // 移除 @Autowired public void setRpcConfig(...) 方法，因为它依赖 Spring 注入。

    /**
     * 获取配置
     */
    public static RpcConfig getRpcConfig(){
        // 如果外部调用时配置尚未初始化，则尝试进行初始化（安全措施）
        if (rpcConfig == null) {
            init();
        }
        return rpcConfig;
    }

}

