package com.baker.gateway.core.netty.processor.filter.error;

import com.baker.gateway.common.constants.ProcessorFilterConstants;
import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.common.exception.GatewayBaseException;
import com.baker.gateway.core.context.Context;
import com.baker.gateway.core.context.GatewayResponse;
import com.baker.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.baker.gateway.core.netty.processor.filter.Filter;
import com.baker.gateway.core.netty.processor.filter.FilterConfig;
import com.baker.gateway.core.netty.processor.filter.ProcessorFilterType;

/**
 * 默认异常处理过滤器，应该放到最后一位
 */
@Filter(
    id = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID,
    name = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_NAME,
    value = ProcessorFilterType.ERROR,
    order = ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ORDER
)
public class DefaultErrorFilter extends AbstractEntryProcessorFilter<FilterConfig> {

    public DefaultErrorFilter() {
        super(FilterConfig.class);
    }

    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        try {
            Throwable throwable = ctx.getThrowable();
            ResponseCode responseCode = ResponseCode.INTERNAL_ERROR;
            if (throwable instanceof GatewayBaseException) {
                GatewayBaseException gatewayBaseException = (GatewayBaseException) throwable;
                responseCode = gatewayBaseException.getCode();
            }
            GatewayResponse gatewayResponse = GatewayResponse.buildGatewayResponse(responseCode);
            ctx.setResponse(gatewayResponse);
        } finally {
            System.err.println("============> do error filter <===============");
            //	设置写回标记
            ctx.writtened();
            //	触发后面的过滤器执行
            super.fireNext(ctx, args);
        }
    }

}
