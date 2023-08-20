package com.baker.gateway.core.context;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DubboRequest {

    //  dubbo服务的注册地址
    private String registriesStr;
    //  dubbo服务的接口名称
    private String interfaceClass;
    //  dubbo服务的方法名
    private String methodName;
    // 	dubbo服务的方法参数签名
    private String[] parameterTypes;
    // 	调用的参数内容
    private Object[] args;
    //	调用的超时时间
    private int timeout;
    // 	调用的版本号
    private String version;


}
