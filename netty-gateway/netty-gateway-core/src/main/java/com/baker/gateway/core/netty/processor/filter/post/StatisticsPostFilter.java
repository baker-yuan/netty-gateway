package com.baker.gateway.core.netty.processor.filter.post;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import com.baker.gateway.common.constants.ProcessorFilterConstants;
import com.baker.gateway.common.metric.Metric;
import com.baker.gateway.common.metric.MetricType;
import com.baker.gateway.common.util.Pair;
import com.baker.gateway.common.util.TimeUtil;
import com.baker.gateway.core.GatewayConfigLoader;
import com.baker.gateway.core.context.Context;
import com.baker.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.baker.gateway.core.netty.processor.filter.Filter;
import com.baker.gateway.core.netty.processor.filter.FilterConfig;
import com.baker.gateway.core.netty.processor.filter.ProcessorFilterType;
import com.baker.gateway.core.rolling.RollingNumber;
import com.baker.gateway.core.rolling.RollingNumberEvent;
import com.baker.gateway.core.plugin.Plugin;
import com.baker.gateway.core.plugin.PluginManager;
import com.baker.gateway.core.plugin.metric.kafka.MetricKafkaClientPlugin;

import lombok.Getter;
import lombok.Setter;

/**
 * 后置过滤器：统计分析
 */
@Filter(
	id = ProcessorFilterConstants.STATISTICS_POST_FILTER_ID,
	name = ProcessorFilterConstants.STATISTICS_POST_FILTER_NAME,
	value = ProcessorFilterType.POST,
	order = ProcessorFilterConstants.STATISTICS_POST_FILTER_ORDER
)
public class StatisticsPostFilter extends AbstractEntryProcessorFilter<StatisticsPostFilter.Config> {

	public static final Integer windowSize = 60 * 1000;
	
	public static final Integer bucketSize = 60;
	
	private final RollingNumber rollingNumber;
	
	private final Thread conusmerThread;
	
	public StatisticsPostFilter() {
		super(StatisticsPostFilter.Config.class);
		MetricConsumer metricConusmer = new MetricConsumer();
		this.rollingNumber = new RollingNumber(windowSize,
				bucketSize,
				"netty-gateway-Gateway",
				metricConusmer.getMetricQueue());
		conusmerThread = new Thread(metricConusmer);
		
	}

	@Override
	public void entry(Context ctx, Object... args) throws Throwable {
		try {
			StatisticsPostFilter.Config config = (StatisticsPostFilter.Config)args[0];
			if(config.isRollingNumber()) {
				conusmerThread.start();
				rollingNumber(ctx, args);
			}
		} finally {
			//	如果走的是最后一个 post filter
			ctx.terminated();
			super.fireNext(ctx, args);
		}
	}
	
	private void rollingNumber(Context ctx, Object... args) {
		Throwable throwable = ctx.getThrowable();
		if(throwable == null) {
			rollingNumber.increment(RollingNumberEvent.SUCCESS);
		}
		else {
			rollingNumber.increment(RollingNumberEvent.FAILURE);
		}
		
		//	请求开始的时间
		long SRTime = ctx.getSRTime();
		//	路由的开始时间 route --> service
		long RSTime = ctx.getRSTime();
		//	路由的接收请求时间 service --> route
		long RRTime = ctx.getRRTime();
		//	请求结束（写出请求的时间）
		long SSTime = ctx.getSSTime();
		
		//	整个生命周期的耗时
		long requestTimeout = SSTime - SRTime;
		long defaultRequestTimeout = GatewayConfigLoader.getGatewayConfig().getRequestTimeout();
		if(requestTimeout > defaultRequestTimeout) {
			rollingNumber.increment(RollingNumberEvent.REQUEST_TIMEOUT);
		}
		
		long routeTimeout = RRTime - RSTime;
		long defaultRouteTimeout = GatewayConfigLoader.getGatewayConfig().getRouteTimeout();
		if(routeTimeout > defaultRouteTimeout) {
			rollingNumber.increment(RollingNumberEvent.ROUTE_TIMEOUT);
		}
	}
	
	@Getter
	@Setter
	public static class Config extends FilterConfig {
		private boolean rollingNumber = true;
	}

	
	public static class MetricConsumer implements Runnable {

		private ArrayBlockingQueue<Pair<String, Long>> metricQueue = new ArrayBlockingQueue<>(65535);
		
		private volatile boolean isRunning = false;
		
		public void start() {
			isRunning = true;
		}
		
		public void shutdown() {
			isRunning = false;
		}
		
		@Override
		public void run() {
			while(isRunning) {
				try {
					Pair<String, Long> pair = metricQueue.take();
					String key = pair.getKey();
					Long value = pair.getValue();
					
					// report 上报
					Plugin plugin = PluginManager.getPlugin().getPlugin(MetricKafkaClientPlugin.class.getName());
					if(plugin != null) {
						MetricKafkaClientPlugin metricKafkaClientPlugin = (MetricKafkaClientPlugin)plugin;
						
						HashMap<String, String> tags = new HashMap<>();
						tags.put(MetricType.KEY, MetricType.STATISTICS);
						
						String topic = GatewayConfigLoader.getGatewayConfig().getMetricTopic();
						
						Metric metric = Metric.create(key, 
								value,
								TimeUtil.currentTimeMillis(),
								tags, 
								topic, 
								false);
						metricKafkaClientPlugin.send(metric);
					}
					
				} catch (InterruptedException e) {
					//	ignore
				}
			}
		}

		public ArrayBlockingQueue<Pair<String, Long>> getMetricQueue() {
			return metricQueue;
		}

		public void setMetricQueue(ArrayBlockingQueue<Pair<String, Long>> metricQueue) {
			this.metricQueue = metricQueue;
		}

		public boolean isRunning() {
			return isRunning;
		}
		
	}

}
