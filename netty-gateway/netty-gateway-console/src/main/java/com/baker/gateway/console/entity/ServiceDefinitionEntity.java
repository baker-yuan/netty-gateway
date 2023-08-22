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



    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        // todo invokerMap比对
        ServiceDefinitionEntity entity = (ServiceDefinitionEntity) obj;
        return Objects.equals(serviceId, entity.getServiceId()) &&
                Objects.equals(basePath, entity.getBasePath()) &&
                Objects.equals(protocol, entity.getProtocol()) &&
                Objects.equals(enable, entity.getEnable()) &&
                Objects.equals(invokerMap, entity.getInvokerMap());
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, basePath, protocol, enable);
    }

}
