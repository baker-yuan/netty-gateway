package com.baker.gateway.common.config;

import com.baker.gateway.common.enums.ResponseCode;
import com.baker.gateway.common.exception.GatewayNotFoundException;
import com.baker.gateway.common.util.Pair;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态服务缓存配置管理类
 */
public class DynamicConfigManager {

    /**
     * 服务的定义集合 key=serviceId(服务的唯一标识) value=资源服务定义
     */
    private final ConcurrentHashMap<String, ServiceDefinition> serviceDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 所有的服务方法信息 key=invokerPath(完整路径) value=服务调用的接口模型描述
     */
    private final ConcurrentHashMap<String, Pair<ServiceInvoker, ServiceDefinition>> allServiceInvokerMap = new ConcurrentHashMap<>();


    /**
     * 服务的实例集合 key=serviceId value=服务实例集合
     */
    private final ConcurrentHashMap<String, Set<ServiceInstance>> serviceInstanceMap = new ConcurrentHashMap<>();

    /**
     * 规则集合 key=ruleId value=规则
     */
    private final ConcurrentHashMap<String, Rule> ruleMap = new ConcurrentHashMap<>();

    private DynamicConfigManager() {
    }

    private static class SingletonHolder {
        private static final DynamicConfigManager INSTANCE = new DynamicConfigManager();
    }


    public static DynamicConfigManager getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /***************** 	对服务定义缓存进行操作的系列方法 	***************/


    public void putServiceDefinition(String serviceId, ServiceDefinition serviceDefinition) {
        serviceDefinitionMap.put(serviceId, serviceDefinition);
        for (Map.Entry<String, ServiceInvoker> entry : serviceDefinition.getInvokerMap().entrySet()) {
            allServiceInvokerMap.put(entry.getKey(), new Pair<>(entry.getValue(), serviceDefinition));
        }

    }

    public ServiceDefinition getServiceDefinition(String serviceId) {
        return serviceDefinitionMap.get(serviceId);
    }

    public void removeServiceDefinition(String serviceId) {
        ServiceDefinition delete = serviceDefinitionMap.remove(serviceId);
        for (String key : delete.getInvokerMap().keySet()) {
            allServiceInvokerMap.remove(key);
        }
    }

    public ConcurrentHashMap<String, ServiceDefinition> getServiceDefinitionMap() {
        return serviceDefinitionMap;
    }


    public Pair<ServiceInvoker, ServiceDefinition> getServiceInvokerMapMap(String invokerPath) {
        return allServiceInvokerMap.get(invokerPath);
    }


    /***************** 	对服务实例缓存进行操作的系列方法 	***************/

    public Set<ServiceInstance> getServiceInstanceByServiceId(String serviceId) {
        return serviceInstanceMap.get(serviceId);
    }

    public void addServiceInstance(String serviceId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> set = serviceInstanceMap.get(serviceId);
        set.add(serviceInstance);
    }

    public void addServiceInstance(String serviceId, Set<ServiceInstance> serviceInstanceSet) {
        serviceInstanceMap.put(serviceId, serviceInstanceSet);
    }

    public void updateServiceInstance(String serviceId, ServiceInstance serviceInstance) {
        Set<ServiceInstance> set = serviceInstanceMap.get(serviceId);
        Iterator<ServiceInstance> it = set.iterator();
        while (it.hasNext()) {
            ServiceInstance is = it.next();
            if (is.getServiceInstanceId().equals(serviceInstance.getServiceInstanceId())) {
                it.remove();
                break;
            }
        }
        set.add(serviceInstance);
    }

    public void removeServiceInstance(String serviceId, String serviceInstanceId) {
        Set<ServiceInstance> set = serviceInstanceMap.get(serviceId);
        Iterator<ServiceInstance> it = set.iterator();
        while (it.hasNext()) {
            ServiceInstance is = it.next();
            if (is.getServiceInstanceId().equals(serviceInstanceId)) {
                it.remove();
                break;
            }
        }
    }

    public void removeServiceInstancesByServiceId(String serviceId) {
        serviceInstanceMap.remove(serviceId);
    }



    /***************** 	对规则缓存进行操作的系列方法 	***************/

    public void putRule(String ruleId, Rule rule) {
        ruleMap.put(ruleId, rule);
    }

    public Rule getRule(String ruleId) {
        Rule rule = ruleMap.get(ruleId);
        if (rule == null) {
            throw new GatewayNotFoundException(ResponseCode.FILTER_CONFIG_PARSE_ERROR);
        }
        return rule;
    }

    public void removeRule(String ruleId) {
        ruleMap.remove(ruleId);
    }

    public ConcurrentHashMap<String, Rule> getRuleMap() {
        return ruleMap;
    }


}
