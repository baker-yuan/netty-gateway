package com.baker.gateway.core.netty;

import com.baker.gateway.core.context.HttpRequestWrapper;
import com.baker.gateway.core.netty.processor.NettyProcessor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty核心处理handler
 *
 * ChannelInboundHandlerAdapter和SimpleChannelInboundHandler都是Netty框架中用于处理网络事件的处理器类，
 * 它们都实现了ChannelInboundHandler接口。以下是它们的主要区别：
 *
 * 1、生命周期管理：SimpleChannelInboundHandler在成功处理数据后会自动释放数据（即调用ReferenceCountUtil.release(msg)）。
 * 这意味着你不需要手动管理数据的生命周期。相反，ChannelInboundHandlerAdapter不会自动释放数据，你需要在你的代码中手动释放它。
 * 2、泛型支持：SimpleChannelInboundHandler是一个泛型类，你可以指定你想要处理的数据的类型。
 * 当你的处理器接收到数据时，Netty会自动将数据转换为你指定的类型，然后调用你的处理器的channelRead0方法。相反，ChannelInboundHandlerAdapter不是一个泛型类，你需要在你的代码中手动转换数据的类型。
 *
 * 总的来说，如果你想要简化你的代码，并且你知道你的处理器将处理的数据的类型，你可以使用SimpleChannelInboundHandler。
 * 如果你需要更多的控制，或者你的处理器需要处理多种类型的数据，你可以使用ChannelInboundHandlerAdapter。
 */
@Slf4j
public class NettyHttpServerHandler extends ChannelInboundHandlerAdapter { // SimpleChannelInboundHandler
	
	private final NettyProcessor nettyProcessor;
	
	public NettyHttpServerHandler(NettyProcessor nettyProcessor) {
		this.nettyProcessor = nettyProcessor;
	}
	
	/**
	 * 核心的请求处理方法
	 *
	 * @see io.netty.channel.ChannelInboundHandlerAdapter#channelRead(io.netty.channel.ChannelHandlerContext, java.lang.Object)
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof HttpRequest) {
			FullHttpRequest request = (FullHttpRequest)msg;
			HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper();
			httpRequestWrapper.setFullHttpRequest(request);
			httpRequestWrapper.setCtx(ctx);
			
			//	processor
			nettyProcessor.process(httpRequestWrapper);
			
		} else {
			//	never go this way, ignore
			log.error("#NettyHttpServerHandler.channelRead# message type is not httpRequest: {}", msg);
			boolean release = ReferenceCountUtil.release(msg);
			if(!release) {
				log.error("#NettyHttpServerHandler.channelRead# release fail 资源释放失败");
			}
		}
	}
}
