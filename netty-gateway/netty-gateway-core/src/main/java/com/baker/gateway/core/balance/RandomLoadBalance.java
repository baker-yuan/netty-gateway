package com.baker.gateway.core.balance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.core.context.GatewayContext;

public class RandomLoadBalance extends AbstractLoadBalance {

    /**
     * 随机负载均衡方法：context instances
     *
     * @see AbstractLoadBalance#doSelect(GatewayContext, java.util.List)
     */
    @Override
    protected ServiceInstance doSelect(GatewayContext context, List<ServiceInstance> instances) {
        //	3
        int length = instances.size();
        //	总权重
        int totalWeight = 0;
        //	是否每个实例的权重都相同
        boolean sameWeight = true;
        //	0 == > 100    1 ==> 80    2 ==> 40
        for (int i = 0; i < length; i++) {
            //	获取真实权重
            int weight = getWeight(instances.get(i));
            //	计算总的权重
            totalWeight += weight;
            //	前后比较权重：有权重不一样的实例, 走权重不一致的逻辑 
            if (sameWeight && i > 0 && weight != getWeight(instances.get(i - 1))) {
                sameWeight = false;
            }
        }
        //	权重不一致的逻辑 : totalWeight = 220
        if (totalWeight > 0 && !sameWeight) {
            // 	根据总权重随机出一个偏移量 offset = 122
            int offset = ThreadLocalRandom.current().nextInt(totalWeight);
            // 	根据偏移量找到靠近偏移量的实例
            for (ServiceInstance instance : instances) {
                //	0 == > 100    1 ==> 80    2 ==> 40
                offset = offset - getWeight(instance);
                if (offset < 0) {
                    return instance;
                }
            }
        }
        // 	如果所有实例权重一致, 使用随机出一个实例即可
        return instances.get(ThreadLocalRandom.current().nextInt(length));
    }
}
