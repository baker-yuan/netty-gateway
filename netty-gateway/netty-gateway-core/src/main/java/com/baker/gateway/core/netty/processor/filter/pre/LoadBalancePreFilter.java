package com.baker.gateway.core.netty.processor.filter.pre;

import java.util.Set;

import com.baker.gateway.common.config.DynamicConfigManager;
import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.common.constants.ProcessorFilterConstants;
import com.baker.gateway.common.constants.GatewayProtocol;
import com.baker.gateway.common.enums.LoadBalanceStrategy;
import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.common.exception.GatewayResponseException;
import com.baker.gateway.core.balance.LoadBalance;
import com.baker.gateway.core.balance.LoadBalanceFactory;
import com.baker.gateway.core.context.AttributeKey;
import com.baker.gateway.core.context.Context;
import com.baker.gateway.core.context.GatewayContext;
import com.baker.gateway.core.context.GatewayRequest;
import com.baker.gateway.core.netty.processor.filter.AbstractEntryProcessorFilter;
import com.baker.gateway.core.netty.processor.filter.FilterConfig;
import com.baker.gateway.core.netty.processor.filter.Filter;
import com.baker.gateway.core.netty.processor.filter.ProcessorFilterType;

import lombok.Getter;
import lombok.Setter;

/**
 * 负载均衡前置过滤器，应该放到第一位
 */
@Filter(
	id = ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_ID,
	name = ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_NAME,
	value = ProcessorFilterType.PRE,
	order = ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_ORDER
)
public class LoadBalancePreFilter extends AbstractEntryProcessorFilter<LoadBalancePreFilter.Config> {

    public LoadBalancePreFilter() {
        super(LoadBalancePreFilter.Config.class);
    }

    @Override
    public void entry(Context ctx, Object... args) throws Throwable {
        try {
            GatewayContext gatewayContext = (GatewayContext) ctx;
            LoadBalancePreFilter.Config config = (LoadBalancePreFilter.Config) args[0];
            LoadBalanceStrategy loadBalanceStrategy = config.getBalanceStrategy();
            String protocol = gatewayContext.getProtocol();
            switch (protocol) {
                case GatewayProtocol.HTTP:
                    // http负载均衡调用
                    doHttpLoadBalance(gatewayContext, loadBalanceStrategy);
                    break;
                case GatewayProtocol.DUBBO:
                    // dubbo负载均衡调用
                    doDubboLoadBalance(gatewayContext, loadBalanceStrategy);
                    break;
                default:
                    break;
            }
        } finally {
            super.fireNext(ctx, args);
        }
    }

    private void doHttpLoadBalance(GatewayContext gatewayContext, LoadBalanceStrategy loadBalanceStrategy) {
        GatewayRequest gatewayRequest = gatewayContext.getRequest();
        String serviceId = gatewayRequest.getServiceId();
        Set<ServiceInstance> serviceInstances = DynamicConfigManager.getInstance()
                .getServiceInstanceByServiceId(serviceId);

        gatewayContext.putAttribute(AttributeKey.MATCH_INSTANCES, serviceInstances);

        //	通过负载均衡枚举值获取负载均衡实例对象
        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(loadBalanceStrategy);
        //	调用负载均衡实现，选择一个实例进行返回
        ServiceInstance serviceInstance = loadBalance.select(gatewayContext);

        if (serviceInstance == null) {
            //	如果服务实例没有找到，终止请求继续执行，显示抛出异常
            gatewayContext.terminated();
            throw new GatewayResponseException(ResponseCode.SERVICE_INSTANCE_NOT_FOUND);
        }

        //	这一步非常关键，设置可修改的服务host，为当前选择的实例对象的address
        gatewayContext.getRequestMutale().setModifyHost(serviceInstance.getAddress());
    }


    private void doDubboLoadBalance(GatewayContext gatewayContext, LoadBalanceStrategy loadBalanceStrategy) {
        //	将负载均衡策略设置到上下文中即可，由 dubbo LoadBalance去进行使用：SPI USED
        gatewayContext.putAttribute(AttributeKey.DUBBO_LOAD_BALANCE_STRATEGY, loadBalanceStrategy);
    }

    /**
     * 负载均衡前置过滤器配置
     */
    @Getter
    @Setter
    public static class Config extends FilterConfig {
        private LoadBalanceStrategy balanceStrategy = LoadBalanceStrategy.ROUND_ROBIN;
    }


}

