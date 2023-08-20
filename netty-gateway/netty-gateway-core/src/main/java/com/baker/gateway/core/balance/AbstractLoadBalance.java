package com.baker.gateway.core.balance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.common.util.CollectionUtils;
import com.baker.gateway.common.util.TimeUtil;
import com.baker.gateway.core.context.AttributeKey;
import com.baker.gateway.core.context.GatewayContext;

/**
 * 抽象负载均衡类：主要实现预热的功能
 */
public abstract class AbstractLoadBalance implements LoadBalance {

	@Override
	public ServiceInstance select(GatewayContext context) {
		
		//	MATCH_INSTANCES：服务实例列表现在还没有填充，需要LoadBalancePreFilter的时候进行获取并设置
		Set<ServiceInstance> matchInstance = context.getAttribute(AttributeKey.MATCH_INSTANCES);
		if(CollectionUtils.isEmpty(matchInstance)) {
			return null;
		}
		
		List<ServiceInstance> instances = new ArrayList<>(matchInstance);
		if(instances.size() == 1) {
			return instances.get(0);
		}
		
		ServiceInstance instance = doSelect(context, instances);
		context.putAttribute(AttributeKey.LOAD_INSTANCE, instance);
		return instance;
	}

	/**
	 * 子类实现指定的负载均衡策略选择一个服务
	 */
	protected abstract ServiceInstance doSelect(GatewayContext context, List<ServiceInstance> instances);

	/**
	 * 获取实例权重，
	 * 服务预热：启动时间在5分钟内的权重逐渐增加，5分钟后权重达到100
	 */
	protected static int getWeight(ServiceInstance instance) {
		int weight = instance.getWeight() == null ? DEFAULT_WEIGHT : instance.getWeight();
		if(weight > 0) {
			//	服务启动注册的时间
			long timestamp = instance.getRegisterTime();
			if(timestamp > 0L) {
				//	服务启动了多久：当前时间 - 注册时间
				int upTime = (int)(TimeUtil.currentTimeMillis() - timestamp);
				//	默认预热时间 5min
				int warmup = DEFAULT_WARMUP;
				if(upTime > 0 && upTime < warmup) {
					weight = calculateWarmUpWeight(upTime, warmup, weight);
				}
			}
		}
		return weight;
	}

	/**
	 * 计算服务在预热时间内的新权重
	 */
	private static int calculateWarmUpWeight(int upTime, int warmup, int weight) {
		int ww =(int)((float)upTime / ((float)warmup / (float) weight));
		return ww < 1 ? 1 : Math.min(ww, weight);
	}
	
}
