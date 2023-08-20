package com.baker.gateway.core.balance;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.core.context.GatewayContext;

/**
 * 负载均衡最上层的接口定义
 */
public interface LoadBalance {
	// 权重
	int DEFAULT_WEIGHT = 100;
	// 默认预热时间
	int DEFAULT_WARMUP = 5 * 60 * 1000;
	
	/**
	 * 从所有实例列表中选择一个实例
	 */
	ServiceInstance select(GatewayContext context);

}
