package com.baker.gateway.core.context;

import com.baker.gateway.common.config.Rule;
import com.baker.gateway.common.util.AssertUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * 网关请求上下文核心对象
 */
public class GatewayContext extends BasicContext {
    /**
     * 核心请求自定义实现
     */
    private final GatewayRequest gatewayRequest;
    /**
     * 网关响应封装类
     */
    private GatewayResponse gatewayResponse;
    /**
     * 规则模型
     */
    private final Rule rule;

    private GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive,
                           GatewayRequest gatewayRequest, Rule rule) {
        super(protocol, nettyCtx, keepAlive);
        this.gatewayRequest = gatewayRequest;
        this.rule = rule;
    }

    /**
     * 建造者类
     */
    public static class Builder {
		/**
		 * 服务的具体协议 http(mvc http) dubbo
		 */
        private String protocol;
		/**
		 * 核心请求自定义实现
		 */
        private GatewayRequest gatewayRequest;
		/**
		 * 规则模型
		 */
        private Rule rule;
		/**
		 *
		 */
        private boolean keepAlive;
		/**
		 *
		 */
		private ChannelHandlerContext nettyCtx;

        public Builder() {
        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder setGatewayRequest(GatewayRequest gatewayRequest) {
            this.gatewayRequest = gatewayRequest;
            return this;
        }

        public Builder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public GatewayContext build() {
            AssertUtil.notNull(protocol, "protocol不能为空");
            AssertUtil.notNull(nettyCtx, "nettyCtx不能为空");
            AssertUtil.notNull(gatewayRequest, "gatewayRequest不能为空");
            AssertUtil.notNull(rule, "rule不能为空");
            return new GatewayContext(protocol, nettyCtx, keepAlive, gatewayRequest, rule);
        }
    }

    /**
     * 获取必要的上下文参数，如果没有则抛出IllegalArgumentException
     *
     * @param key 必须要存在的
     */
    public <T> T getRequiredAttribute(AttributeKey<T> key) {
        T value = getAttribute(key);
        AssertUtil.notNull(value, "required attribute '" + key + "' is missing !");
        return value;
    }

    /**
     * 获取指定key的上下文参数，如果没有则返回第二个参数的默认值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttributeOrDefault(AttributeKey<T> key, T defaultValue) {
        return (T) attributes.getOrDefault(key, defaultValue);
    }

    /**
     * 根据过滤器id获取对应的过滤器配置信息
     */
    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    /**
     * 获取服务Id
     */
    public String getServiceId() {
        return gatewayRequest.getServiceId();
    }

    /**
     * 重写覆盖父类，basicContext的该方法，主要用于真正的释放操作
     *
     * @see BasicContext#releaseRequest()
     */
    @Override
    public void releaseRequest() {
        if (requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(gatewayRequest.getFullHttpRequest());
        }
    }

    @Override
    public Rule getRule() {
        return rule;
    }

    @Override
    public GatewayRequest getRequest() {
        return gatewayRequest;
    }

    /**
     * 调用该方法就是获取原始请求内容，不去做任何修改动作
     */
    public GatewayRequest getOriginRequest() {
        return gatewayRequest;
    }

    /**
     * 调用该方法区分于原始的请求对象操作，主要就是做属性修改的
     */
    public GatewayRequest getRequestMutable() {
        return gatewayRequest;
    }

    @Override
    public GatewayResponse getResponse() {
        return gatewayResponse;
    }

    @Override
    public void setResponse(Object response) {
        this.gatewayResponse = (GatewayResponse) response;
    }

}
