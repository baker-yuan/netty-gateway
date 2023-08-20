package com.baker.gateway.core.netty.processor;

import com.baker.gateway.core.GatewayConfig;
import com.lmax.disruptor.dsl.ProducerType;
import com.baker.gateway.common.concurrent.queue.flusher.ParallelFlusher;
import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.core.context.HttpRequestWrapper;
import com.baker.gateway.core.helper.ResponseHelper;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * flusher缓冲队列的核心实现，最终调用的方法还是要回归到NettyCoreProcessor
 */
@Slf4j
public class NettyBatchEventProcessor implements NettyProcessor {
	
	private static final String THREAD_NAME_PREFIX = "netty-gateway-flusher-";
	
	private final GatewayConfig gatewayConfig;
	
	private final NettyCoreProcessor nettyCoreProcessor;
	
	private final ParallelFlusher<HttpRequestWrapper> parallelFlusher;
	
	public NettyBatchEventProcessor(GatewayConfig gatewayConfig, NettyCoreProcessor nettyCoreProcessor) {
		this.gatewayConfig = gatewayConfig;
		this.nettyCoreProcessor = nettyCoreProcessor;
		ParallelFlusher.Builder<HttpRequestWrapper> builder = new ParallelFlusher.Builder<HttpRequestWrapper>()
				.setBufferSize(gatewayConfig.getBufferSize())
				.setThreads(gatewayConfig.getProcessThread())
				.setProducerType(ProducerType.MULTI)
				.setNamePrefix(THREAD_NAME_PREFIX)
				.setWaitStrategy(gatewayConfig.getATureWaitStrategy());
		
		BatchEventProcessorListener batchEventProcessorListener = new BatchEventProcessorListener();
		builder.setEventListener(batchEventProcessorListener);
		this.parallelFlusher = builder.build();
	}

	@Override
	public void process(HttpRequestWrapper httpRequestWrapper) {
		this.parallelFlusher.add(httpRequestWrapper);
	}

	@Override
	public void start() {
		this.nettyCoreProcessor.start();
		this.parallelFlusher.start();		
	}

	@Override
	public void shutdown() {
		this.nettyCoreProcessor.shutdown();
		this.parallelFlusher.shutdown();
	}
	
	/**
	 * 监听事件的处理核心逻辑
	 */
	public class BatchEventProcessorListener implements ParallelFlusher.EventListener<HttpRequestWrapper> {

		@Override
		public void onEvent(HttpRequestWrapper event) throws Exception {
			nettyCoreProcessor.process(event);
		}

		@Override
		public void onException(Throwable t, long sequence, HttpRequestWrapper event) {
			HttpRequest request = event.getFullHttpRequest();
			ChannelHandlerContext ctx = event.getCtx();
			try {
				log.error("#BatchEventProcessorListener# onException 请求处理失败, request: {}. errorMessage: {}", 
						request, t.getMessage(), t);
				
				//	首先构建响应对象
				FullHttpResponse fullHttpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
				//	判断是否保持连接
				if(!HttpUtil.isKeepAlive(request)) {
					ctx.writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
				} else {
					//	如果保持连接, 则需要设置一下响应头：key: CONNECTION,  value: KEEP_ALIVE
					fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
					ctx.writeAndFlush(fullHttpResponse);
				}
				
			} catch (Exception e) {
				//	ignore
				log.error("#BatchEventProcessorListener# onException 请求回写失败, request: {}. errorMessage: {}", 
						request, e.getMessage(), e);
			}
		}
	}

	public GatewayConfig getGatewayConfig() {
		return gatewayConfig;
	}
	
	
	
}
