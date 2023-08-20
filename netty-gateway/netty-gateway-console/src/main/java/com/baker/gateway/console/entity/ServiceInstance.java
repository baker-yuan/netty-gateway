package com.baker.gateway.console.entity;

import lombok.*;


/**
 * 服务实例，一个服务定义会对应多个服务实例
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInstance  {
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
}
