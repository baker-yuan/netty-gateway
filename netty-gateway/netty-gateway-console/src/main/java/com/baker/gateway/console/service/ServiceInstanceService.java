package com.baker.gateway.console.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.baker.gateway.common.util.Pair;
import com.baker.gateway.discovery.api.RegistryService;

@Service
public class ServiceInstanceService {

	@Value("${gateway.console.namespace}")
	private String namespace;

	@Autowired
	private RegistryService registryService;



	public void addOrUpdate(ServiceInstance serviceInstance) {

	}


	/**
	 * 根据服务唯一标识获取实例列表
	 */
	public List<ServiceInstance> getServiceInstanceList(String prefixPath, String serviceId) throws Exception {
		/**
		 * 		/netty-gateway-env
		 * 			/services
		 * 				/serviceA:1.0.0		==>	value: ServiceDefinition & AbstractServiceInvoker
		 * 				/serviceB:1.0.0
		 * 			/instances
		 * 			/instances/serviceA:1.0.0/192.168.11.100:port  	==>  value: ServiceInstance1
		 * 			/instances/serviceA:1.0.0/192.168.11.101:port		==>  value: ServiceInstance1
		 * 			/instances/serviceB:1.0.0	
		 * 				/192.168.11.103:port  	==>  value: ServiceInstance1	
		 * 			/routes
		 * 				/uuid01	==>	value: Rule1
		 * 				/uuid02 ==>	value: Rule2
		 */
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.INSTANCE_PREFIX
				+ RegistryService.PATH 
				+ serviceId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		
		List<ServiceInstance> serviceInstances = new ArrayList<>();

		for(Pair<String, String> pair : list) {
			// String p = pair.getObject1();
			// if (p.equals(path)) {
			// 	continue;
			// }
			String json = pair.getValue();
			ServiceInstance si = FastJsonConvertUtil.convertJSONToObject(json, ServiceInstance.class);
			serviceInstances.add(si);
		}
		
		return serviceInstances;
	}
	
	public void updateEnable(String prefixPath, String serviceId, String serviceInstanceId, boolean enable) throws Exception {
		updateServiceInstance(prefixPath, serviceId, serviceInstanceId, enable);
	}
	
	public void updateTags(String prefixPath, String serviceId, String serviceInstanceId, String tags) throws Exception {
		updateServiceInstance(prefixPath, serviceId, serviceInstanceId, tags);
	}
	
	public void updateWeight(String prefixPath, String serviceId, String serviceInstanceId, int weight) throws Exception {
		updateServiceInstance(prefixPath, serviceId, serviceInstanceId, weight);
	}

	/**
	 * 启用禁用某个服务实例
	 */
	private void updateServiceInstance(String prefixPath, String serviceId, String serviceInstanceId, Object param) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.INSTANCE_PREFIX
				+ RegistryService.PATH 
				+ serviceId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);

		for(Pair<String, String> pair : list) {
			String p = pair.getKey();
			if (p.equals(path)) { 
				continue;
			}
			String json = pair.getValue();
			ServiceInstance si = FastJsonConvertUtil.convertJSONToObject(json, ServiceInstance.class);
			//	更新启用禁用
			if((si.getServiceInstanceId()).equals(serviceInstanceId)) {

				//	update:  tags & enable & weight
				if(param instanceof String) {
					String tags = (String)param;
					si.setTags(tags);
				}
				if(param instanceof Boolean) {
					boolean enable = (boolean)param;
					si.setEnable(enable);
				}
				if(param instanceof Integer) {
					int weight = (int)param;
					si.setWeight(weight);
				}
				
			}
			//	会写数据到ETCD
			String value = FastJsonConvertUtil.convertObjectToJSON(si);
			registryService.registerEphemeralNode(p, value);
		}
	}


}
