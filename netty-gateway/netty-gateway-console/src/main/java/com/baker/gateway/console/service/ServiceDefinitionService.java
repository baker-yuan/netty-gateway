package com.baker.gateway.console.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baker.gateway.common.config.Rule;
import com.baker.gateway.common.util.JSONUtil;
import com.baker.gateway.console.entity.RuleEntity;
import com.baker.gateway.console.entity.ServiceDefinitionEntity;
import com.baker.gateway.console.mapper.ServiceDefinitionMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baker.gateway.common.config.ServiceDefinition;
import com.baker.gateway.common.config.ServiceInvoker;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.baker.gateway.common.util.Pair;
import com.baker.gateway.discovery.api.RegistryService;


@Service
public class ServiceDefinitionService {

	@Value("${gateway.console.namespace}")
	private String namespace;

	@Autowired
	private RegistryService registryService;

	@Autowired
	private ServiceDefinitionMapper serviceDefinitionMapper;

	public List<ServiceDefinition> getServiceDefinitionByDb() {
		List<ServiceDefinition> result = Lists.newArrayList();
		List<ServiceDefinitionEntity> definitionEntityList = serviceDefinitionMapper.selectAll();
		for (ServiceDefinitionEntity definition : definitionEntityList) {
			result.add(entityToModel(definition));
		}
		return result;
	}

	public void addOrUpdateServiceDefinitionToDb(ServiceDefinition serviceDefinition) {
		ServiceDefinitionEntity entity = modelToEntity(serviceDefinition);
		if (serviceDefinition.getServiceId() == null) {
			serviceDefinitionMapper.insert(entity);
		} else {
			serviceDefinitionMapper.update(entity);
		}
	}

	public void deleteServiceDefinition(String serviceId) {
		serviceDefinitionMapper.delete(serviceId);
	}


	private ServiceDefinition entityToModel(ServiceDefinitionEntity serviceDefinitionEntity) {
		ServiceDefinition serviceDefinition = new ServiceDefinition();
		serviceDefinition.setServiceId(serviceDefinitionEntity.getServiceId());
		serviceDefinition.setBasePath(serviceDefinitionEntity.getBasePath());
		serviceDefinition.setProtocol(serviceDefinitionEntity.getProtocol());
		serviceDefinition.setEnable(serviceDefinitionEntity.getEnable());
		serviceDefinition.setInvokerMap(JSONUtil.parse(serviceDefinitionEntity.getInvokerMap(), new TypeReference<Map<String, ServiceInvoker>>() {}));
		return serviceDefinition;
	}

	private ServiceDefinitionEntity modelToEntity(ServiceDefinition definition) {
		ServiceDefinitionEntity entity = new ServiceDefinitionEntity();
		entity.setServiceId(definition.getServiceId());
		entity.setBasePath(definition.getBasePath());
		entity.setProtocol(definition.getProtocol());
		entity.setEnable(definition.getEnable());
		entity.setInvokerMap(JSONUtil.toJSONString(definition.getInvokerMap()));
		return entity;
	}




	/** ------------------------------- -------------------------------**/

	/**
	 * 根据前缀获取服务定义列表
	 */
	public List<ServiceDefinition> getServiceDefinitionList() throws Exception {
		String path = RegistryService.PATH 
				+ namespace
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
	
	public void updatePatternPathByServiceId(String serviceId, String patternPath) throws Exception {
		updateServiceDefinitionByServiceId(serviceId, false);
	}
	
	public void updateEnableByServiceId(String serviceId, boolean enable) throws Exception {
		updateServiceDefinitionByServiceId(serviceId, enable);
	}

	/**
	 * 根据服务唯一ID 更新patternPath enable
	 */
	private void updateServiceDefinitionByServiceId(String serviceId, Object param) throws Exception {
		String path = RegistryService.PATH 
				+ namespace
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
	public List<ServiceInvoker> getServiceInvokerByServiceId(String serviceId) throws Exception {
		String path = RegistryService.PATH 
				+ namespace
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
	public void serviceInvokerBindingRuleId(String serviceId, String invokerPath, String ruleId) throws Exception {
		String path = RegistryService.PATH 
				+ namespace
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
