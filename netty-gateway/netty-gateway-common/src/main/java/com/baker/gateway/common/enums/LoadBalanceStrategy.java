package com.baker.gateway.common.enums;

/**
 * 负载均衡策略
 */
public enum LoadBalanceStrategy {
    RANDOM("RANDOM","随机负载均衡策略"),
    ROUND_ROBIN("ROUND_ROBIN","轮询负载均衡策略");

    private final String val;
    private final String desc;

    LoadBalanceStrategy(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }

    public String getVal() {
        return val;
    }

    public String getDesc() {
        return desc;
    }
}
