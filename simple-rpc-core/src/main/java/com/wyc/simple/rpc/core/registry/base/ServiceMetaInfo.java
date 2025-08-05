package com.wyc.simple.rpc.core.registry.base;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 服务元信息（用于注册中心有关服务）
 */
@Data
public class ServiceMetaInfo {


    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号（从 SimpleRpcConfig 中获取）
     */
    private String serviceVersion = "1.0.0";

    /**
     * 服务分组
     */
    private String serviceGroup = "default";

    /**
     * 服务域名
     */
    private String serviceHost;

    /**
     * 服务端口号
     */
    private Integer servicePort;

    /**
     * 获取服务
     * 服务名称 + 版本号
     * @return
     */
    public String getServiceKey() {
        return String.format("%s-%s", serviceName, serviceVersion);
    }

    /**
     * 获取服务节点
     * 服务名称 + 服务版本号 + 节点地址
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }

    /**
     * 节点调用地址
     * @return
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }

}
