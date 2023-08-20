package com.baker.gateway.core.balance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.core.context.GatewayContext;


public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private ConcurrentMap<String /*path*/, ConcurrentMap<String /*address*/, WeightedRoundRobin>> pathWeightMap = new ConcurrentHashMap<>();

    //	实例未更新时长阈值，默认为60秒；若未更新时长超过阈值后会移除掉
    private static final int RECYCLE_PERIOD = 60000;


    @Override
    protected ServiceInstance doSelect(GatewayContext context, List<ServiceInstance> instances) {
        //	原生请求路径
    	String path = context.getOriginRequest().getPath();
    	//	如果不存在该path则创建一个map集合
        ConcurrentMap<String, WeightedRoundRobin> map = pathWeightMap.computeIfAbsent(path, key -> new ConcurrentHashMap<>());
        
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        long now = System.currentTimeMillis();
        ServiceInstance selectedInstance = null;
        WeightedRoundRobin selectedWRR = null;
        for (ServiceInstance instance : instances) {
        	//	获取服务实例地址
            String address = instance.getAddress();
            //	获取服务实例权重
            int weight = getWeight(instance);
            //	如果地址不存在则加入到map里WeightedRoundRobin对象
            WeightedRoundRobin weightedRoundRobin = map.computeIfAbsent(address, k -> {
                WeightedRoundRobin wrr = new WeightedRoundRobin();
                //	初始化设置weight, current
                wrr.setWeight(weight);
                return wrr;
            });
            
            //	当前权重与缓存权重比对, 如果权重发生变化则进行更新weight 并初始化当前权重current
            if (weight != weightedRoundRobin.getWeight()) {
                weightedRoundRobin.setWeight(weight);
            }
            //	设置当前权重、更新时间
            long cur = weightedRoundRobin.increaseCurrent();
            weightedRoundRobin.setLastUpdate(now);
            //	轮询比对, 设置当前最大的权重maxCurrent、以及所对应的实例selectedInstance、缓存的对象selectedWRR
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedInstance = instance;
                selectedWRR = weightedRoundRobin;
            }
            //	总权重累计
            totalWeight += weight;
        }
        //	如果当前实例个数和缓存不匹配, 则进行清除过期的实例缓存
        if (instances.size() != map.size()) {
            map.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > RECYCLE_PERIOD);
        }
        //	返回权重最大的实例, 并设置当前权重为最小值： A -> B -> C
        if (selectedInstance != null) {
            selectedWRR.sel(totalWeight);
            return selectedInstance;
        }
        return instances.get(0);
    }
    
    /**
     * 权重缓存对象
     */
    protected static class WeightedRoundRobin {
        //	实例权重
        private int weight;
        //	当前权重
        private AtomicLong current = new AtomicLong(0);
        //	最后一次更新时间
        private long lastUpdate;

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            //	初始化
            current.set(0);
        }

        public long increaseCurrent() {
            //  current = current + weight;
            return current.addAndGet(weight);
        }

        public void sel(int total) {
            //  current = current - total;
            current.addAndGet(-1 * total);
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }

}
