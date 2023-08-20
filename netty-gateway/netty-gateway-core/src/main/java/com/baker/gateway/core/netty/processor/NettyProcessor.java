package com.baker.gateway.core.netty.processor;

import com.baker.gateway.core.context.HttpRequestWrapper;

/**
 * 处理Netty核心逻辑的执行器接口定义
 */
public interface NettyProcessor {

	/**
	 * 核心执行方法
	 */
	void process(HttpRequestWrapper httpRequestWrapper) throws Exception;
	
	/**
	 * 执行器启动方法
	 */
	void start();
	
	/**
	 * 执行器资源释放/关闭方法
	 */
	void shutdown();
	
}
