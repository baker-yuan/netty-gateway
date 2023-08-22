package com.baker.gateway.console.service;


import com.baker.gateway.discovery.api.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.baker.gateway.discovery.api.RegistryService;

@Service
public class ServiceInstanceService {

	@Value("${gateway.console.namespace}")
	private String namespace;

	@Autowired
	private RegistryService registryService;

	public void report(ServiceInstance serviceInstance) throws Exception {
		// todo 分布式锁

		registerServiceInstance(serviceInstance);
	}

	/**
	 * 注册服务实例方法
	 */
	protected void registerServiceInstance(ServiceInstance serviceInstance) throws Exception {
		String key = Registry.PATH
				+ namespace
				+ Registry.INSTANCE_PREFIX
				+ Registry.PATH
				+ serviceInstance.getServiceId()
				+ Registry.PATH
				+ serviceInstance.getServiceInstanceId();
		if(!registryService.isExistKey(key)) {
			String value = FastJsonConvertUtil.convertObjectToJSON(serviceInstance);
			registryService.registerPathIfNotExists(key, value, false);
		}
	}

}
