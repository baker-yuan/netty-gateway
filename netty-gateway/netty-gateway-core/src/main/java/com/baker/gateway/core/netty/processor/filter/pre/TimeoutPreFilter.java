package com.baker.gateway.core.netty.processor.filter.pre;

import com.baker.gateway.common.config.DubboServiceInvoker;
import com.baker.gateway.common.constants.ProcessorFilterConstants;
import com.baker.gateway.common.constants.GatewayProtocol;
import com.baker.gateway.core.context.AttributeKey;
import com.baker.gateway.core.context.Context;
import com.baker.gateway.core.context.GatewayContext;
import com.baker.gateway.core.context.GatewayRequest;
import com.baker.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.baker.gateway.core.netty.processor.filter.FilterConfig;
import com.baker.gateway.core.netty.processor.filter.ProcessorFilter;
import com.baker.gateway.core.netty.processor.filter.Filter;
import com.baker.gateway.core.netty.processor.filter.ProcessorFilterType;

import lombok.Getter;
import lombok.Setter;

/**
 * 超时的前置过滤器
 */
@Filter(
	id = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ID,
	name = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_NAME,
	value = ProcessorFilterType.PRE,
	order = ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ORDER
)
public class TimeoutPreFilter extends AbstractEntryProcessorFilter<TimeoutPreFilter.Config> {

    public TimeoutPreFilter() {
        super(TimeoutPreFilter.Config.class);
    }

    /**
     * 超时的过滤器核心方法实现
     *
     * @see ProcessorFilter#entry(java.lang.Object, java.lang.Object[])
     */
    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        try {
            GatewayContext gatewayContext = (GatewayContext) ctx;
            String protocol = gatewayContext.getProtocol();
            TimeoutPreFilter.Config config = (TimeoutPreFilter.Config) args[0];
            switch (protocol) {
                case GatewayProtocol.HTTP:
                    GatewayRequest gatewayRequest = gatewayContext.getRequest();
                    gatewayRequest.setRequestTimeout(config.getTimeout());
                    break;
                case GatewayProtocol.DUBBO:
                    DubboServiceInvoker dubboServiceInvoker = (DubboServiceInvoker) gatewayContext.getRequiredAttribute(AttributeKey.DUBBO_INVOKER);
                    dubboServiceInvoker.setTimeout(config.getTimeout());
                    break;
                default:
                    break;
            }
        } finally {
            //	非常重要的，一定要记得，驱动我们的过滤器链表
            super.fireNext(ctx, args);
        }
    }

    @Getter
    @Setter
    public static class Config extends FilterConfig {
        private Integer timeout;
    }

}
