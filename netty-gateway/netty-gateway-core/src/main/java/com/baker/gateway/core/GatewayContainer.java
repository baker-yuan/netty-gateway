package com.baker.gateway.core;

import com.baker.gateway.common.constants.GatewayBufferHelper;
import com.baker.gateway.core.netty.NettyHttpClient;
import com.baker.gateway.core.netty.NettyHttpServer;
import com.baker.gateway.core.netty.processor.NettyBatchEventProcessor;
import com.baker.gateway.core.netty.processor.NettyCoreProcessor;
import com.baker.gateway.core.netty.processor.NettyMpmcProcessor;
import com.baker.gateway.core.netty.processor.NettyProcessor;

import lombok.extern.slf4j.Slf4j;

/**
 * 主流程的容器类
 */
@Slf4j
public class GatewayContainer implements LifeCycle {
	/**
	 * 核心配置类
	 */
	private final GatewayConfig gatewayConfig;
	/**
	 * 核心处理器
	 */
	private NettyProcessor nettyProcessor;
	/**
	 * 接收http请求的server，接收到请求后交给NettyProcessor处理
	 */
	private NettyHttpServer nettyHttpServer;
	/**
	 * http转发的核心类
	 */
	private NettyHttpClient nettyHttpClient;

	public GatewayContainer(GatewayConfig gatewayConfig) {
		this.gatewayConfig = gatewayConfig;
		init();
	}
	
	@Override
	public void init() {
		//	1. 构建核心处理器
		NettyCoreProcessor nettyCoreProcessor = new NettyCoreProcessor();
		
		//	2. 是否开启缓存
		String bufferType = gatewayConfig.getBufferType();
		if(GatewayBufferHelper.isFlusher(bufferType)) {
			nettyProcessor = new NettyBatchEventProcessor(gatewayConfig, nettyCoreProcessor);
		}
		else if(GatewayBufferHelper.isMpmc(bufferType)) {
			nettyProcessor = new NettyMpmcProcessor(gatewayConfig, nettyCoreProcessor, true);
		}
		else {
			nettyProcessor = nettyCoreProcessor;
		}

		//	3. 创建NettyHttpServer
		nettyHttpServer = new NettyHttpServer(gatewayConfig, nettyProcessor);
		
		//	4. 创建NettyHttpClient
		nettyHttpClient = new NettyHttpClient(gatewayConfig, nettyHttpServer.getEventLoopGroupWork());
	}

	@Override
	public void start() {
		nettyProcessor.start();
		nettyHttpServer.start();
		nettyHttpClient.start();
		log.info("GatewayContainer started !");
	}

	@Override
	public void shutdown() {
		nettyProcessor.shutdown();
		nettyHttpServer.shutdown();
		nettyHttpClient.shutdown();
	}

}
