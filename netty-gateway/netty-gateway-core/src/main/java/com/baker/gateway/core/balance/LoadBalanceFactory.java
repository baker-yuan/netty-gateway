package com.baker.gateway.core.balance;

import java.util.HashMap;
import java.util.Map;

import com.baker.gateway.common.enums.LoadBalanceStrategy;


public class LoadBalanceFactory {

    private final Map<LoadBalanceStrategy, LoadBalance> loadBalanceMap = new HashMap<>();

    private static final LoadBalanceFactory INSTANCE = new LoadBalanceFactory();

    private LoadBalanceFactory() {
        loadBalanceMap.put(LoadBalanceStrategy.RANDOM, new RandomLoadBalance());
        loadBalanceMap.put(LoadBalanceStrategy.ROUND_ROBIN, new RoundRobinLoadBalance());
    }

    public static LoadBalance getLoadBalance(LoadBalanceStrategy loadBalance) {
        return INSTANCE.loadBalanceMap.get(loadBalance);
    }

}
