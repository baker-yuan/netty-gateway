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
			String p = pair.getObject1();
			if (p.equals(path)) { 
				continue;
			}
			String json = pair.getObject2();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			serviceDefinitions.add(sd);
		}
		return serviceDefinitions;
	}
	
	public void updatePatternPathByUniqueId(String prefixPath, String uniqueId, String patternPath) throws Exception {
		updateServiceDefinitionByUniqueId(prefixPath, uniqueId, false);
	}
	
	public void updateEnableByUniqueId(String prefixPath, String uniqueId, boolean enable) throws Exception {
		updateServiceDefinitionByUniqueId(prefixPath, uniqueId, enable);
	}

	/**
	 * 根据服务唯一ID 更新patternPath enable
	 */
	private void updateServiceDefinitionByUniqueId(String prefixPath, String uniqueId, Object param) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX
				+ RegistryService.PATH 
				+ uniqueId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		if(list.size() == 1) {
			Pair<String, String> pair = list.get(0);
			String key = pair.getObject1();
			String json = pair.getObject2();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			//	update:  patternPath & enable
			if(param instanceof String) {
				String patternPath = (String)param;
				sd.setPatternPath(patternPath);
			}
			if(param instanceof Boolean) {
				boolean enable = (boolean)param;
				sd.setEnable(enable);
			}
			String value = FastJsonConvertUtil.convertObjectToJSON(sd);
			registryService.registerPersistentNode(key, value);
		}
	}

	/**
	 * 根据uniqueId获取指定的服务下的调用方法列表
	 */
	public List<ServiceInvoker> getServiceInvokerByUniqueId(String prefixPath, String uniqueId) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX
				+ RegistryService.PATH 
				+ uniqueId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		List<ServiceInvoker> invokerList = new ArrayList<>();
		if(list.size() == 1) {
			Pair<String, String> pair = list.get(0);
			String json = pair.getObject2();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			Map<String, ServiceInvoker> map = sd.getInvokerMap();
			invokerList.addAll(map.values());
		}
		return invokerList;
	}

	/**
	 * 为ServiceInvoker绑定一个规则ID
	 */
	public void serviceInvokerBindingRuleId(String prefixPath, String uniqueId, String invokerPath, String ruleId) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX
				+ RegistryService.PATH 
				+ uniqueId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		if(list.size() == 1) {
			Pair<String, String> pair = list.get(0);
			String key = pair.getObject1();
			String json = pair.getObject2();
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
