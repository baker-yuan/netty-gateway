package com.baker.gateway.core.netty.processor;

import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.common.exception.GatewayNotFoundException;
import com.baker.gateway.common.exception.GatewayPathNoMatchedException;
import com.baker.gateway.common.exception.GatewayResponseException;
import com.baker.gateway.core.context.HttpRequestWrapper;
import com.baker.gateway.core.context.GatewayContext;
import com.baker.gateway.core.helper.RequestHelper;
import com.baker.gateway.core.helper.ResponseHelper;
import com.baker.gateway.core.netty.processor.filter.DefaultProcessorFilterFactory;
import com.baker.gateway.core.netty.processor.filter.ProcessorFilterFactory;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 核心流程的主执行逻辑
 */
@Slf4j
public class NettyCoreProcessor implements NettyProcessor {
	/**
	 * 过滤器工厂接口
	 */
	private final ProcessorFilterFactory processorFilterFactory = DefaultProcessorFilterFactory.getInstance();
	
	@Override
	public void process(HttpRequestWrapper event) {
		FullHttpRequest request = event.getFullHttpRequest();
		ChannelHandlerContext ctx = event.getCtx();
		try {
			//	1. 解析FullHttpRequest，把他转换为我们自己想要的内部对象，Context
			GatewayContext gatewayContext = RequestHelper.doContext(request, ctx);
			
			//	2. 执行整个的过滤器逻辑，FilterChain
			processorFilterFactory.doFilterChain(gatewayContext);
		} catch (GatewayPathNoMatchedException e) {
			log.error("#NettyCoreProcessor# process 网关资指定路径为匹配异常，快速失败： code: {}, msg: {}", 
					e.getCode().getCode(), e.getCode().getMessage(), e);
			FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
			//	释放资源写回响应
			doWriteAndRelease(ctx, request, response);			
		} catch(GatewayNotFoundException e) {
			log.error("#NettyCoreProcessor# process 网关资源未找到异常： code: {}, msg: {}", 
					e.getCode().getCode(), e.getCode().getMessage(), e);
			FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
			//	释放资源写回响应
			doWriteAndRelease(ctx, request, response);
		} catch(GatewayResponseException e) {
			log.error("#NettyCoreProcessor# process 网关内部未知错误异常： code: {}, msg: {}",
					e.getCode().getCode(), e.getCode().getMessage(), e);
			FullHttpResponse response = ResponseHelper.getHttpResponse(e.getCode());
			//	释放资源写回响应
			doWriteAndRelease(ctx, request, response);
		} catch (Throwable t) {
			log.error("#NettyCoreProcessor# process 网关内部未知错误异常", t);
			FullHttpResponse response = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
			//	释放资源写回响应
			doWriteAndRelease(ctx, request, response);
		}
	}

	/**
	 * 写回响应信息并释放资源
	 */
	private void doWriteAndRelease(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		boolean release = ReferenceCountUtil.release(request);
		if(!release) {
			log.warn("#NettyCoreProcessor# doWriteAndRelease release fail 释放资源失败， request:{}", request.uri());
		}
	}

	@Override
	public void start() {
	}

	@Override
	public void shutdown() {
	}

}
