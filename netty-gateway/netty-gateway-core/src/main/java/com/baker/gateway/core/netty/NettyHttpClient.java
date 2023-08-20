package com.baker.gateway.core.netty;

import java.io.IOException;

import com.baker.gateway.core.GatewayConfig;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import com.baker.gateway.core.LifeCycle;
import com.baker.gateway.core.helper.AsyncHttpHelper;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP客户端启动类，主要用于下游服务的请求转发
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {

	private AsyncHttpClient asyncHttpClient;
	
	private DefaultAsyncHttpClientConfig.Builder clientBuilder;
	
	private final GatewayConfig gatewayConfig;
	
	private final EventLoopGroup eventLoopGroupWork;
	
	public NettyHttpClient(GatewayConfig gatewayConfig, EventLoopGroup eventLoopGroupWork) {
		this.gatewayConfig = gatewayConfig;
		this.eventLoopGroupWork = eventLoopGroupWork;
		//	在构造函数调用初始化方法
		init();
	}
	
	/**
	 * 初始化AsyncHttpClient
	 */
	@Override
	public void init() {
		this.clientBuilder = new DefaultAsyncHttpClientConfig.Builder()
				.setFollowRedirect(false)
				.setEventLoopGroup(eventLoopGroupWork)
				.setConnectTimeout(gatewayConfig.getHttpConnectTimeout())
				.setRequestTimeout(gatewayConfig.getHttpRequestTimeout())
				.setMaxRequestRetry(gatewayConfig.getHttpMaxRequestRetry())
				.setAllocator(PooledByteBufAllocator.DEFAULT)
				.setCompressionEnforced(true)
				.setMaxConnections(gatewayConfig.getHttpMaxConnections())
				.setMaxConnectionsPerHost(gatewayConfig.getHttpConnectionsPerHost())
				.setPooledConnectionIdleTimeout(gatewayConfig.getHttpPooledConnectionIdleTimeout());
	}

	@Override
	public void start() {

		this.asyncHttpClient = new DefaultAsyncHttpClient(clientBuilder.build());

		// 异步的http辅助类
		AsyncHttpHelper.getInstance().initialized(asyncHttpClient);
	}

	@Override
	public void shutdown() {
		if(asyncHttpClient != null) {
			try {
				this.asyncHttpClient.close();
			} catch (IOException e) {
				// ignore
				log.error("#NettyHttpClient.shutdown# shutdown error", e);
			}
		}
	}

}
