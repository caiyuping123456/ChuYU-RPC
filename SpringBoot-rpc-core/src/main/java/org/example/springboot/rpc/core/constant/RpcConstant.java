package org.example.springboot.rpc.core.constant;

import java.time.Duration;

public interface RpcConstant {
    /**
     *
     默认配置文件加载前缀
     */
    String DEFAULT_CONFIG_PREFIX = "rpc";
    /**
     *
     默认服务版本
     */
    String DEFAULT_SERVICE_VERSION = "1.0";
    /**
     * 过期时间
     * 30秒
     */
    Duration MAX_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 过期时间
     * 30秒过期
     */
    Long TIME_OUT = 30L;
}
