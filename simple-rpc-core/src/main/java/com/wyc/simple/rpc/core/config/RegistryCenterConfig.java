package com.wyc.simple.rpc.core.config;

import com.wyc.simple.rpc.core.registry.constant.RegistryCenterKeys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RPC 框架注册中心配置
 */
@Data
@ConfigurationProperties(prefix = RegistryCenterConfig.PREFIX)
@Configuration
public class RegistryCenterConfig {

    public static final String PREFIX = "simple-rpc.registry-center-config";

    /**
     * 注册中心类别
     */
    private String registry = RegistryCenterKeys.ETCD;

    /**
     * 注册中心地址
     */
    private String address = "http://localhost:2379";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间
     */
    private Long timeout = 10000L;
}
