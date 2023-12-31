package com.baker.gateway.common.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务实例，一个服务定义会对应多个服务实例
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ServiceInstance implements Serializable {

    private static final long serialVersionUID = -7559569289189228478L;

    /**
     * 服务实例ID: ip:port
     */
    protected String serviceInstanceId;

    /**
     * 服务定义唯一id
     */
    protected String serviceId;

    /**
     * 服务实例地址： ip:port
     */
    protected String address;

    /**
     * 标签信息
     */
    protected String tags;

    /**
     * 权重信息
     */
    protected Integer weight;

    /**
     * 服务注册的时间戳，后面我们做负载均衡，warmup预热
     */
    protected long registerTime;

    /**
     * 服务实例启用禁用
     */
    protected boolean enable = true;

    /**
     * 服务实例对应的版本号
     */
    protected String version;

    public ServiceInstance(String serviceInstanceId, String serviceId, String address, String tags, Integer weight,
                           long registerTime, boolean enable, String version) {
        this.serviceInstanceId = serviceInstanceId;
        this.serviceId = serviceId;
        this.address = address;
        this.tags = tags;
        this.weight = weight;
        this.registerTime = registerTime;
        this.enable = enable;
        this.version = version;
    }

}
