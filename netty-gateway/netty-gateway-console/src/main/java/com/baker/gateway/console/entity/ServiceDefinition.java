package com.baker.gateway.console.entity;

import lombok.*;

import java.util.Map;

/**
 * 资源服务定义类，无论下游是什么样的服务都需要进行注册
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode
public class ServiceDefinition {
    /**
     * 服务唯一id
     */
    private String serviceId;

    /**
     * 服务URL前缀，全局唯一
     */
    private String basePath;

    /**
     * 服务的版本号
     */
    private String version;

    /**
     * 服务的具体协议 http、dubbo、grpc
     */
    private String protocol;

    /**
     * 服务启用禁用
     */
    private boolean enable = true;

    /**
     * 服务方法信息 key=invokerPath(完整路径) value=服务调用的接口模型描述
     */
    private Map<String, ServiceInvoker> httpInvokerMap;
    private Map<String, DubboServiceInvoker> dubboInvokerMap;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class  ServiceInvoker {
        /**
         * 方法全路径
         */
        protected String invokerPath;
        /**
         * 规则id
         */
        protected String ruleId;
        /**
         * 调用接口超时时间
         */
        protected int timeout = 5000;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class  DubboServiceInvoker extends ServiceInvoker {
        /**
         * 注册中心地址
         */
        private String registerAddress;

        /**
         * 接口全类名
         */
        private String interfaceClass;

        /**
         * 方法名称
         */
        private String methodName;

        /**
         * 参数名字的集合
         */
        private String[] parameterTypes;

        /**
         * dubbo服务的版本号
         */
        private String version;
    }

}
