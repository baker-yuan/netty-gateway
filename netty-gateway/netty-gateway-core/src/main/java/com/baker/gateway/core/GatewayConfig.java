package com.baker.gateway.core;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.baker.gateway.common.constants.BasicConst;
import com.baker.gateway.common.constants.GatewayBufferHelper;
import com.baker.gateway.common.util.NetUtils;

import lombok.Data;

/**
 * 网关的通用配置信息类
 */
@Data
public class GatewayConfig {
	// 网关的默认端口
	private int port = 8888;

	// 网关服务唯一ID eg：192.168.11.111:8888
	private String gatewayId = NetUtils.getLocalIp() + BasicConst.COLON_SEPARATOR + port;

	// 网关的注册中心地址
	private String registryAddress = "http://192.168.11.114:2379,http://192.168.11.115:2379,http://192.168.11.116:2379";

	// 网关的命名空间
	private String namespace = "netty-gateway";

	// env dev test prod
	private String env = "dev";

	// 网关服务器的CPU核数映射的线程数
	private int processThread = Runtime.getRuntime().availableProcessors();

	// Netty的Boss线程数
	private int eventLoopGroupBossNum = 1;
	
	//	Netty的Work线程数
	private int eventLoopGroupWorkNum = processThread;
	
	// 是否开启EPOLL
	private boolean useEPoll = true;
	
	// 是否开启Netty内存分配机制
	private boolean nettyAllocator = true;
	
	// http body报文最大大小
	private int maxContentLength = 64 * 1024 * 1024;
	
	// dubbo开启连接数数量
	private int dubboConnections = processThread;
	
	// 设置响应模式, 默认是单异步模式：CompletableFuture回调处理结果： whenComplete  or  whenCompleteAsync
	private boolean whenComplete = true;
	
	// 网关队列：缓冲模式
	private String bufferType = GatewayBufferHelper.MPMC;
	
	// 网关队列：内存队列大小
	private int bufferSize = 1024 * 16;
	
	// 网关队列：阻塞/等待策略
	private String waitStrategy = "blocking";
	
	// 默认请求超时时间 3s
	private long requestTimeout = 3000;
	
	// 默认路由转发的慢调用时间 2s
	private long routeTimeout = 2000;
	
    // kafka地址
    private String kafkaAddress = "";//"192.168.11.51:9092";
    
    // 网关服务指标消息主题
    private String metricTopic = "netty-gateway-metric-topic";
	
	public WaitStrategy getATureWaitStrategy() {
		switch (waitStrategy) {
			case "blocking":
				return new BlockingWaitStrategy();
			case "busySpin":
				return new BusySpinWaitStrategy();
			case "yielding":
				return new YieldingWaitStrategy();
			case "sleeping":
				return new SleepingWaitStrategy();
			default:
				return new BlockingWaitStrategy();
		}
	}
	
	//	Http Async 参数选项：

	// 连接超时时间
	private int httpConnectTimeout = 30 * 1000;
	// 请求超时时间
	private int httpRequestTimeout = 30 * 1000;
	// 客户端请求重试次数
	private int httpMaxRequestRetry = 2;
	// 客户端请求最大连接数
	private int httpMaxConnections = 10000;
	// 客户端每个地址支持的最大连接数
	private int httpConnectionsPerHost = 8000;
	// 客户端空闲连接超时时间, 默认60秒
	private int httpPooledConnectionIdleTimeout = 60 * 1000;
}
