package com.baker.gateway.core.netty.processor.filter.route;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.baker.gateway.core.GatewayConfigLoader;
import com.baker.gateway.core.context.Context;
import com.baker.gateway.core.context.GatewayContext;
import com.baker.gateway.core.context.GatewayResponse;
import com.baker.gateway.core.helper.AsyncHttpHelper;
import com.baker.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.baker.gateway.core.netty.processor.filter.Filter;
import com.baker.gateway.core.netty.processor.filter.FilterConfig;
import com.baker.gateway.core.netty.processor.filter.ProcessorFilterType;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import com.baker.gateway.common.constants.ProcessorFilterConstants;
import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.common.exception.GatewayConnectException;
import com.baker.gateway.common.exception.GatewayResponseException;
import com.baker.gateway.common.util.TimeUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 请求路由的中置过滤器
 */
@Filter(
    id = ProcessorFilterConstants.HTTP_ROUTE_FILTER_ID,
    name = ProcessorFilterConstants.HTTP_ROUTE_FILTER_NAME,
    value = ProcessorFilterType.ROUTE,
    order = ProcessorFilterConstants.HTTP_ROUTE_FILTER_ORDER
)
@Slf4j
public class HttpRouteFilter extends AbstractEntryProcessorFilter<FilterConfig> {

    public HttpRouteFilter() {
        super(FilterConfig.class);
    }

    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        GatewayContext gatewayContext = (GatewayContext) ctx;
        Request request = gatewayContext.getRequestMutale().build();

        // 设置RS
        gatewayContext.setRSTime(TimeUtil.currentTimeMillis());

        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);

        // 双异步和单异步模式
        boolean whenComplete = GatewayConfigLoader.getGatewayConfig().isWhenComplete();
        //	单异步模式
        if (whenComplete) {
            future.whenComplete((response, throwable) -> complete(request, response, throwable, gatewayContext, args));
        }
        //	双异步模式
        else {
            future.whenCompleteAsync((response, throwable) -> complete(request, response, throwable, gatewayContext, args));
        }
    }

    /**
     * 真正执行请求响应回来的操作方法
     */
    private void complete(Request request, Response response, Throwable throwable, GatewayContext context, Object... args) {
        try {
            //	设置RR
            context.setRRTime(TimeUtil.currentTimeMillis());

            //	1. 释放请求资源
            context.releaseRequest();

            //	2. 判断是否有异常产生
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                //	超时异常
                if (throwable instanceof java.util.concurrent.TimeoutException) {
                    log.warn("#HttpRouteFilter# complete返回响应执行， 请求路径：{}，耗时超过 {}  ms.",
                            url,
                            (request.getRequestTimeout() == 0 ?
                                    GatewayConfigLoader.getGatewayConfig().getHttpRequestTimeout() :
                                    request.getRequestTimeout())
                    );
                    //	网关里设置异常都是使用自定义异常
                    context.setThrowable(new GatewayResponseException(ResponseCode.REQUEST_TIMEOUT));
                }
                //	其他异常情况
                else {
                    context.setThrowable(new GatewayConnectException(throwable,
                            context.getUniqueId(),
                            url,
                            ResponseCode.HTTP_RESPONSE_ERROR));
                }
            }
            //	正常返回响应结果
            else {
                //	设置响应信息
                context.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } catch (Throwable t) {
            //	最终兜底异常处理
            context.setThrowable(new GatewayResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("#HttpRouteFilter# complete catch到未知异常", t);
        } finally {
            try {
                //	1.	设置写回标记
                context.writtened();

                //	2. 	让异步线程内部自己进行触发下一个节点执行
                super.fireNext(context, args);
            } catch (Throwable t) {
                //	兜底处理，把异常信息放入上下文
                context.setThrowable(new GatewayResponseException(ResponseCode.INTERNAL_ERROR));
                log.error("#HttpRouteFilter# fireNext出现异常", t);
            }
        }
    }

}
