package com.baker.gateway.common.config;

import java.util.Objects;

/**
 * http协议的注册服务调用模型类
 */
public class HttpServiceInvoker extends AbstractServiceInvoker {
    public boolean bizEquals(HttpServiceInvoker obj) {
        return Objects.equals(this.invokerPath, obj.getInvokerPath()) && this.timeout == obj.getTimeout();
    }
}
