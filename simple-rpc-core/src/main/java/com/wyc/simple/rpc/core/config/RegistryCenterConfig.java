package com.wyc.simple.rpc.core.config;

import com.wyc.simple.rpc.core.registry.constant.RegistryCenterKeys;
import lombok.Data;

/**
 * RPC 框架注册中心配置
 */
@Data
public class RegistryCenterConfig {

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
