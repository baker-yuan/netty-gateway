package com.baker.gateway.core.balance;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Constants;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.common.enums.LoadBalanceStrategy;
import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.common.exception.GatewayResponseException;
import com.baker.gateway.core.context.AttributeKey;
import com.baker.gateway.core.context.GatewayContext;
import com.baker.gateway.core.helper.DubboReferenceHelper;

/**
 * 使用dubbo的SPI扩展点实现
 */
public class DubboLoadBalance implements org.apache.dubbo.rpc.cluster.LoadBalance {

	public static final String NAME = "rlb";
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
		System.err.println("---------------- DubboLoadBalance into  --------------");
		
		GatewayContext gatewayContext = (GatewayContext)RpcContext.getContext().get(DubboReferenceHelper.DUBBO_TRANSFER_CONTEXT);
		LoadBalanceStrategy loadBalanceStrategy = gatewayContext.getAttribute(AttributeKey.DUBBO_LOAD_BALANCE_STRATEGY);
		LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(loadBalanceStrategy);
		Set<ServiceInstance> instanceWrappers = new HashSet<>();
		for(Invoker<?> invoker : invokers) {
			instanceWrappers.add(new ServiceInstanceWrapper<>(invoker, invocation));
		}
		// 	把dubbo invokers的服务实例列表 转成自己能够认识的ServiceInstance，设置到全局上下文对象里
		gatewayContext.putAttribute(AttributeKey.MATCH_INSTANCES, instanceWrappers);
		
		ServiceInstance serviceInstance = loadBalance.select(gatewayContext);
		if(serviceInstance instanceof ServiceInstanceWrapper) {
			return ((ServiceInstanceWrapper)serviceInstance).getInvoker();
		} else {
			//	永远不会走
			throw new GatewayResponseException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
		}
	}
	
	public static class ServiceInstanceWrapper<T> extends ServiceInstance {
		
		private static final long serialVersionUID = -6254823227724967507L;

		private final Invoker<T> invoker;
		
		public ServiceInstanceWrapper(Invoker<T> invoker, Invocation invocation) {
			this.invoker = invoker;
			this.setServiceInstanceId(invoker.getUrl().getAddress());
			this.setAddress(invoker.getUrl().getAddress());
			this.setUniqueId(invoker.getUrl().getServiceKey());
			this.setRegisterTime(invoker.getUrl().getParameter(CommonConstants.TIMESTAMP_KEY, 0L));
			this.setWeight(invoker.getUrl().getMethodParameter(invocation.getMethodName(),
					Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT));
			this.setVersion(invoker.getUrl().getParameter(CommonConstants.VERSION_KEY));
			this.setEnable(true);
		}

		public Invoker<T> getInvoker() {
			return invoker;
		}
		
		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(getClass() != o.getClass()) return false;
			ServiceInstanceWrapper<?> serviceInstanceWrapper = (ServiceInstanceWrapper<?>)o;
			return Objects.equals(this.address, serviceInstanceWrapper.address);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(this.address);
		}
		
	}

}
