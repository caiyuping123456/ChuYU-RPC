package org.example.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

public class ConfigUtils {
    public static <T> T loadConfig(Class<T> tClass,String prefix){
        return loadConfig(tClass, prefix,"");
    }

    private static <T> T loadConfig(Class<T> tClass, String prefix, String enviroment) {
        StringBuilder configBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(enviroment)) {
            configBuilder.append("-").append(enviroment);
        }
        configBuilder.append(".properties");
        //获取配置信息
        Props props = new Props(configBuilder.toString());
        //将properties中的对应的前缀映射到tClass中。
        return props.toBean(tClass, prefix);
    }
}
