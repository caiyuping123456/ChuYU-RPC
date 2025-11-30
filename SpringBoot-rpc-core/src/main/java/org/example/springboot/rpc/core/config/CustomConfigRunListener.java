package org.example.springboot.rpc.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;

// 注意：这个类不能被 Spring 扫描，它需要通过特定的方式注册。
@Slf4j
public class CustomConfigRunListener implements SpringApplicationRunListener {

    private static RpcConfig GLOBAL_RPC_CONFIG;

    // 构造器是必需的，签名必须匹配 (SpringApplication, String[])
    public CustomConfigRunListener(SpringApplication application, String[] args) {
        // 构造器是 Spring Boot 调用的
    }

    // 3. 静态方法：供 RpcInitBootstrap 调用
    public static RpcConfig getGlobalRpcConfig() {
        if (GLOBAL_RPC_CONFIG == null) {
            throw new IllegalStateException("RpcConfig 尚未初始化。请检查 CustomConfigRunListener 是否已正确注册和执行。");
        }
        return GLOBAL_RPC_CONFIG;
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        SpringApplicationRunListener.super.starting(bootstrapContext);
    }

    /**
     * 关键方法：在 Environment 加载完成后，但 BeanFactory 尚未初始化时执行
     */
    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        // 1. 手动读取配置
        RpcConfig config = new RpcConfig();

        // 使用 environment.getProperty 读取 YAML/Properties 中的键
        String name = environment.getProperty("rpc.name", "ChuYu-rpc");
        String version = environment.getProperty("rpc.version","1.0");
        Integer port = environment.getProperty("rpc.serverPort", Integer.class, 8080);
        String  serverHost = environment.getProperty("rpc.serverHost", "localhost");
        Boolean mock = environment.getProperty("rpc.mock", Boolean.class,false);
        String serializer = environment.getProperty("rpc.serializer","json");
        String retryStrategy = environment.getProperty("rpc.retryStrategy","fixedInterval");

        // --- 嵌套对象加载 (rpc.registryConfig) ---
        RegistryConfig registryConfig = new RegistryConfig();

        // rpc.registryConfig.registry
        String registry = environment.getProperty("rpc.registryConfig.registry", "etcd");
        registryConfig.setRegistry(registry);

        // rpc.registryConfig.address
        String address = environment.getProperty("rpc.registryConfig.address", "http://localhost:2379"); // 默认值使用 etcd 默认端口
        registryConfig.setAddress(address);

        // rpc.registryConfig.username
        // 对于凭证类信息，通常不设置硬编码的默认值
        String username = environment.getProperty("rpc.registryConfig.username");
        registryConfig.setUsername(username);

        // rpc.registryConfig.password
        String password = environment.getProperty("rpc.registryConfig.password");
        registryConfig.setPassword(password);

        // rpc.registryConfig.timeout
        Integer timeout = environment.getProperty("rpc.registryConfig.timeout", Integer.class, 10000);
        registryConfig.setTimeout(Long.valueOf(timeout));

        config.setName(name);
        config.setVersion(version);
        config.setMock(mock);
        config.setServerHost(serverHost);
        config.setServerPort(port);
        config.setSerializer(serializer);
        config.setRetryStrategy(retryStrategy);
        config.setRegistryConfig(registryConfig);

        // 2. 将配置存储在静态变量中，供 ImportBeanDefinitionRegistrar 访问
        GLOBAL_RPC_CONFIG = config;

        log.info("[RPC INIT] Global RpcConfig loaded early: " ,config);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        SpringApplicationRunListener.super.contextPrepared(context);
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        SpringApplicationRunListener.super.contextLoaded(context);
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        SpringApplicationRunListener.super.started(context, timeTaken);
    }

    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
        SpringApplicationRunListener.super.ready(context, timeTaken);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        SpringApplicationRunListener.super.failed(context, exception);
    }
}