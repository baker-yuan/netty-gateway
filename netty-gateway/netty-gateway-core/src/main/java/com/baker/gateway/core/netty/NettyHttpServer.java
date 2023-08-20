package com.baker.gateway.core.netty;

import java.net.InetSocketAddress;

import com.baker.gateway.common.util.RemotingHelper;
import com.baker.gateway.common.util.RemotingUtil;
import com.baker.gateway.core.GatewayConfig;
import com.baker.gateway.core.LifeCycle;
import com.baker.gateway.core.netty.processor.NettyProcessor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 承接所有网络请求的核心类
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {
	/**
	 * 网关的通用配置信息类
	 */
	private final GatewayConfig gatewayConfig;
	/**
	 * netty监听端口
	 */
	private int port = 8888;

	/**
	 * 配置和启动ServerChannel
	 */
	private ServerBootstrap serverBootstrap;
	/**
	 * 接受来自客户端的连接
	 */
	private EventLoopGroup eventLoopGroupBoss;
	/**
	 * 处理已接受的连接
	 */
	private EventLoopGroup eventLoopGroupWork;

	/**
	 * 处理Netty核心逻辑的执行器接口定义
	 */
	private NettyProcessor nettyProcessor;
	
	public NettyHttpServer(GatewayConfig gatewayConfig, NettyProcessor nettyProcessor) {
		this.gatewayConfig = gatewayConfig;
		this.nettyProcessor = nettyProcessor;
		if(gatewayConfig.getPort() > 0 && gatewayConfig.getPort() < 65535) {
			this.port = gatewayConfig.getPort();
		}
		//	初始化NettyHttpServer
		init();
	}

	/**
	 * 初始化方法
	 * @see LifeCycle#init()
	 */
	@Override
	public void init() {
		this.serverBootstrap = new ServerBootstrap();
		if(useEPoll()) {
			this.eventLoopGroupBoss = new EpollEventLoopGroup(gatewayConfig.getEventLoopGroupBossNum(),
					new DefaultThreadFactory("NettyBossEPoll"));
			this.eventLoopGroupWork = new EpollEventLoopGroup(gatewayConfig.getEventLoopGroupWorkNum(),
					new DefaultThreadFactory("NettyWorkEPoll"));
		} else {
			this.eventLoopGroupBoss = new NioEventLoopGroup(gatewayConfig.getEventLoopGroupBossNum(),
					new DefaultThreadFactory("NettyBossNio"));
			this.eventLoopGroupWork = new NioEventLoopGroup(gatewayConfig.getEventLoopGroupWorkNum(),
					new DefaultThreadFactory("NettyWorkNio"));
		}
	}
	
	/**
	 * 判断是否支持EPoll
	 */
	public boolean useEPoll() {
		return gatewayConfig.isUseEPoll() && RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
	}

	/**
	 * 服务器启动方法
	 * @see LifeCycle#start()
	 */
	@Override
	public void start() {
		ServerBootstrap handler = this.serverBootstrap
		 		.group(eventLoopGroupBoss, eventLoopGroupWork)
		 		.channel(useEPoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
		 		.option(ChannelOption.SO_BACKLOG, 1024)		// sync + accept = backlog
		 		.option(ChannelOption.SO_REUSEADDR, true)   	// tcp端口重绑定
		 		.option(ChannelOption.SO_KEEPALIVE, false)  	// 如果在两小时内没有数据通信的时候，TCP会自动发送一个活动探测数据报文
		 		.childOption(ChannelOption.TCP_NODELAY, true)  // 该参数的左右就是禁用Nagle算法，使用小数据传输时合并
		 		.childOption(ChannelOption.SO_SNDBUF, 65535)	 // 设置发送数据缓冲区大小
		 		.childOption(ChannelOption.SO_RCVBUF, 65535)	 // 设置接收数据缓冲区大小
		 		.localAddress(new InetSocketAddress(this.port))
		 		.childHandler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel ch) {
						ch.pipeline().addLast(
								new HttpServerCodec(),
								new HttpObjectAggregator(gatewayConfig.getMaxContentLength()),
								new HttpServerExpectContinueHandler(),
								new NettyServerConnectManagerHandler(),
								new NettyHttpServerHandler(nettyProcessor)
								);
					}
				});
		
		if(gatewayConfig.isNettyAllocator()) {
			handler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		}
		
		try {
			this.serverBootstrap.bind().sync();
			log.info("< ============= Gateway Server StartUp On Port: " + this.port + "================ >");
		} catch (Exception e) {
			throw new RuntimeException("this.serverBootstrap.bind().sync() fail!", e);
		}
	}
	
	/**
	 * 关闭
	 * @see LifeCycle#shutdown()
	 */
	@Override
	public void shutdown() {
		if(eventLoopGroupBoss != null) {
			eventLoopGroupBoss.shutdownGracefully();
		}
		if(eventLoopGroupWork != null) {
			eventLoopGroupWork.shutdownGracefully();
		}
	}

	/**
	 * 获取NettyHttpServer的EventLoopGroupWork
	 */
	public EventLoopGroup getEventLoopGroupWork() {
		return eventLoopGroupWork;
	}


	/**
	 * 连接管理器
	 */
	static class NettyServerConnectManagerHandler extends ChannelDuplexHandler {
		
	    @Override
	    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
	    	final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
	    	log.debug("NETTY SERVER PIPLINE: channelRegistered {}", remoteAddr);
	    	super.channelRegistered(ctx);
	    }

	    @Override
	    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
	    	final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
	    	log.debug("NETTY SERVER PIPLINE: channelUnregistered {}", remoteAddr);
	    	super.channelUnregistered(ctx);
	    }

	    @Override
	    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	    	final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
	    	log.debug("NETTY SERVER PIPLINE: channelActive {}", remoteAddr);
	    	super.channelActive(ctx);
	    }

	    @Override
	    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	    	final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
	    	log.debug("NETTY SERVER PIPLINE: channelInactive {}", remoteAddr);
	    	super.channelInactive(ctx);
	    }
		
	    @Override
	    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
	    	if(evt instanceof IdleStateEvent) {
	    		IdleStateEvent event = (IdleStateEvent)evt;
	    		if(event.state().equals(IdleState.ALL_IDLE)) {
	    	    	final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
	    	    	log.warn("NETTY SERVER PIPLINE: userEventTriggered: IDLE {}", remoteAddr);
	    	    	ctx.channel().close();
	    		}
	    	}
	    	ctx.fireUserEventTriggered(evt);
	    }

	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	    	final String remoteAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
	    	log.warn("NETTY SERVER PIPLINE: remoteAddr： {}, exceptionCaught {}", remoteAddr, cause);
	    	ctx.channel().close();
	    }
		
	}

}
