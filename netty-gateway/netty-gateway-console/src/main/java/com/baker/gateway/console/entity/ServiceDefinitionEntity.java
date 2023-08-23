package com.baker.gateway.console.entity;

import lombok.*;

import java.util.Objects;

/**
 * 资源服务定义类，无论下游是什么样的服务都需要进行注册
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDefinitionEntity {
    /**
     * 服务唯一id
     */
    private String serviceId;

    /**
     * 服务URL前缀，全局唯一
     */
    private String basePath;

    /**
     * 服务的具体协议 http、dubbo、grpc
     */
    private String protocol;

    /**
     * 服务启用禁用
     */
    private Boolean enable = true;

    /**
     * 服务方法信息 key=invokerPath(完整路径) value=服务调用的接口模型描述
     */
    private String invokerMap;

    /**
     * 草稿
     */
    private String draft;




}
