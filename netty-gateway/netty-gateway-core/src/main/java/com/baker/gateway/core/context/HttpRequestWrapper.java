package com.baker.gateway.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * 请求包装类
 */
@Data
public class HttpRequestWrapper {

	private FullHttpRequest fullHttpRequest;
	
	private ChannelHandlerContext ctx;
	
}
