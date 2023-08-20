package com.baker.gateway.core.netty.processor.filter.route;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.baker.gateway.common.config.DubboServiceInvoker;
import com.baker.gateway.common.config.ServiceInvoker;
import com.baker.gateway.common.constants.ProcessorFilterConstants;
import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.common.exception.DubboConnectException;
import com.baker.gateway.common.exception.GatewayResponseException;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.baker.gateway.common.util.TimeUtil;
import com.baker.gateway.core.GatewayConfigLoader;
import com.baker.gateway.core.helper.DubboReferenceHelper;
import com.baker.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.baker.gateway.core.netty.processor.filter.Filter;
import com.baker.gateway.core.netty.processor.filter.FilterConfig;
import com.baker.gateway.core.netty.processor.filter.ProcessorFilterType;
import com.baker.gateway.core.context.AttributeKey;
import com.baker.gateway.core.context.Context;
import com.baker.gateway.core.context.DubboRequest;
import com.baker.gateway.core.context.GatewayContext;
import com.baker.gateway.core.context.GatewayResponse;

import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;


@Filter(
    id = ProcessorFilterConstants.DUBBO_ROUTE_FILTER_ID,
    name = ProcessorFilterConstants.DUBBO_ROUTE_FILTER_NAME,
    value = ProcessorFilterType.ROUTE,
    order = ProcessorFilterConstants.DUBBO_ROUTE_FILTER_ORDER
)
@Slf4j
public class DubboRouteFilter extends AbstractEntryProcessorFilter<FilterConfig> {

    public DubboRouteFilter() {
        super(FilterConfig.class);
    }

    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        GatewayContext gatewayContext = (GatewayContext) ctx;
        ServiceInvoker serviceInvoker = gatewayContext.getRequiredAttribute(AttributeKey.DUBBO_INVOKER);
        DubboServiceInvoker dubboServiceInvoker = (DubboServiceInvoker) serviceInvoker;

        //	请求协议的校验
        if (!HttpHeaderValues.APPLICATION_JSON.toString().equals(gatewayContext.getOriginRequest().getContentType())) {
            //	显示抛出异常 必须要终止执行
            gatewayContext.terminated();
            throw new GatewayResponseException(ResponseCode.DUBBO_PARAMETER_VALUE_ERROR);
        }

        String body = gatewayContext.getOriginRequest().getBody();

        // 这一步的时候就可以是否请求对象
        gatewayContext.releaseRequest();

        List<Object> parameters;
        try {
            parameters = FastJsonConvertUtil.convertJSONToArray(body, Object.class);
        } catch (Exception e) {
            //	如果解析异常
            gatewayContext.terminated();
            throw new GatewayResponseException(ResponseCode.DUBBO_PARAMETER_VALUE_ERROR);
        }

        //	构建dubbo请求对象
        DubboRequest dubboRequest = DubboReferenceHelper.buildDubboRequest(dubboServiceInvoker, parameters.toArray());

        //	设置RS:
        gatewayContext.setRSTime(TimeUtil.currentTimeMillis());

        CompletableFuture<Object> future = DubboReferenceHelper.getInstance().$invokeAsync(gatewayContext, dubboRequest);

        //	双异步和单异步模式
        boolean whenComplete = GatewayConfigLoader.getGatewayConfig().isWhenComplete();

        //	单异步模式
        if (whenComplete) {
            future.whenComplete((response, throwable) -> {
                complete(dubboServiceInvoker, response, throwable, gatewayContext, args);
            });
        }
        //	双异步模式
        else {
            future.whenCompleteAsync((response, throwable) -> {
                complete(dubboServiceInvoker, response, throwable, gatewayContext, args);
            });
        }
    }

    /**
     * 回调响应处理实现
     */
    private void complete(DubboServiceInvoker dubboServiceInvoker,
                          Object response,
                          Throwable throwable,
                          GatewayContext gatewayContext,
                          Object[] args) {
        try {
            //	设置RR
            gatewayContext.setRRTime(TimeUtil.currentTimeMillis());

            if (Objects.nonNull(throwable)) {
                DubboConnectException dubboConnectException = new DubboConnectException(throwable,
                        gatewayContext.getServiceId(),
                        gatewayContext.getOriginRequest().getPath(),
                        dubboServiceInvoker.getInterfaceClass(),
                        dubboServiceInvoker.getMethodName(),
                        ResponseCode.DUBBO_RESPONSE_ERROR);
                gatewayContext.setThrowable(dubboConnectException);
            } else {
                GatewayResponse gatewayResponse = GatewayResponse.buildGatewayResponseObj(response);
                gatewayContext.setResponse(gatewayResponse);
            }

        } catch (Throwable t) {
            //	最终兜底异常处理
            gatewayContext.setThrowable(new GatewayResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("#DubboRouteFilter# complete catch到未知异常", t);
        } finally {
            try {
                //	1.	设置写回标记
                gatewayContext.writtened();
                //	2. 	让异步线程内部自己进行触发下一个节点执行
                super.fireNext(gatewayContext, args);
            } catch (Throwable t) {
                //	兜底处理，把异常信息放入上下文
                gatewayContext.setThrowable(new GatewayResponseException(ResponseCode.INTERNAL_ERROR));
                log.error("#DubboRouteFilter# fireNext出现异常", t);
            }
        }
    }

}
