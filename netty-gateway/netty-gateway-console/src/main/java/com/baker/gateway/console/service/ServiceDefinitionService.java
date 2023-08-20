package com.baker.gateway.console.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baker.gateway.common.config.ServiceDefinition;
import com.baker.gateway.common.config.ServiceInvoker;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.baker.gateway.common.util.Pair;
import com.baker.gateway.discovery.api.RegistryService;


@Service
public class ServiceDefinitionService {

	@Autowired
	private RegistryService registryService;
	
	/**
	 * 根据前缀获取服务定义列表
	 */
	public List<ServiceDefinition> getServiceDefinitionList(String prefixPath) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		List<ServiceDefinition> serviceDefinitions = new ArrayList<>();
		for(Pair<String, String> pair : list) {
			String p = pair.getKey();
			if (p.equals(path)) { 
				continue;
			}
			String json = pair.getValue();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			serviceDefinitions.add(sd);
		}
		return serviceDefinitions;
	}
	
	public void updatePatternPathByServiceId(String prefixPath, String serviceId, String patternPath) throws Exception {
		updateServiceDefinitionByServiceId(prefixPath, serviceId, false);
	}
	
	public void updateEnableByServiceId(String prefixPath, String serviceId, boolean enable) throws Exception {
		updateServiceDefinitionByServiceId(prefixPath, serviceId, enable);
	}

	/**
	 * 根据服务唯一ID 更新patternPath enable
	 */
	private void updateServiceDefinitionByServiceId(String prefixPath, String serviceId, Object param) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX
				+ RegistryService.PATH 
				+ serviceId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		if(list.size() == 1) {
			Pair<String, String> pair = list.get(0);
			String key = pair.getKey();
			String json = pair.getValue();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			if(param instanceof Boolean) {
				boolean enable = (boolean)param;
				sd.setEnable(enable);
			}
			String value = FastJsonConvertUtil.convertObjectToJSON(sd);
			registryService.registerPersistentNode(key, value);
		}
	}

	/**
	 * 根据serviceId获取指定的服务下的调用方法列表
	 */
	public List<ServiceInvoker> getServiceInvokerByServiceId(String prefixPath, String serviceId) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX
				+ RegistryService.PATH 
				+ serviceId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		List<ServiceInvoker> invokerList = new ArrayList<>();
		if(list.size() == 1) {
			Pair<String, String> pair = list.get(0);
			String json = pair.getValue();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			Map<String, ServiceInvoker> map = sd.getInvokerMap();
			invokerList.addAll(map.values());
		}
		return invokerList;
	}

	/**
	 * 为ServiceInvoker绑定一个规则ID
	 */
	public void serviceInvokerBindingRuleId(String prefixPath, String serviceId, String invokerPath, String ruleId) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX
				+ RegistryService.PATH 
				+ serviceId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		if(list.size() == 1) {
			Pair<String, String> pair = list.get(0);
			String key = pair.getKey();
			String json = pair.getValue();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			Map<String, ServiceInvoker> map = sd.getInvokerMap();
			for(Map.Entry<String, ServiceInvoker> entry : map.entrySet()) {
				String pathKey = entry.getKey();
				ServiceInvoker invokerValue = entry.getValue();
				if(pathKey.equals(invokerPath)) {
					//	绑定ruleId
					invokerValue.setRuleId(ruleId);
				}
			}
			String value = FastJsonConvertUtil.convertObjectToJSON(sd);
			registryService.registerPersistentNode(key, value);
		}
	}
	
}
