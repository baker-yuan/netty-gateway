package com.baker.gateway.console.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.baker.gateway.common.config.DubboServiceInvoker;
import com.baker.gateway.common.config.HttpServiceInvoker;
import com.baker.gateway.common.constants.GatewayProtocol;
import com.baker.gateway.common.util.JSONUtil;
import com.baker.gateway.common.util.Pair;
import com.baker.gateway.console.entity.ServiceDefinitionEntity;
import com.baker.gateway.console.mapper.ServiceDefinitionMapper;
import com.baker.gateway.discovery.api.Registry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baker.gateway.common.config.ServiceDefinition;
import com.baker.gateway.common.config.ServiceInvoker;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.baker.gateway.discovery.api.RegistryService;


@Service
public class ServiceDefinitionService {

	@Value("${gateway.console.namespace}")
	private String namespace;

	@Autowired
	private RegistryService registryService;

	@Autowired
	private ServiceDefinitionMapper serviceDefinitionMapper;


	public void report(ServiceDefinition serviceDefinition) throws Exception {
		// todo 分布式锁

		// todo url前缀校验

		ServiceDefinitionEntity dbEntity = serviceDefinitionMapper.selectById(serviceDefinition.getServiceId());
		// 第一次上报，也需要手动确认上线，为了逻辑统一draft也存了数据
		if (dbEntity == null) {
			ServiceDefinitionEntity entity = modelToEntity(serviceDefinition);
			entity.setDraft(JSONUtil.toJSONString(serviceDefinition));
			serviceDefinitionMapper.insert(entity);
			return;
		}

		// 没有修改到任何字段
		ServiceDefinitionEntity updateEntity = modelToEntity(serviceDefinition);
		if (dbEntity.equals(updateEntity)) {
			return;
		}

		// 保存到草稿中，手动发布上线
		dbEntity.setDraft(JSONUtil.toJSONString(updateEntity));
		serviceDefinitionMapper.update(dbEntity);
	}


	/**
	 * 发布
	 */
	public void publish(String serviceId) throws Exception {
		ServiceDefinitionEntity dbEntity = serviceDefinitionMapper.selectById(serviceId);
		ServiceDefinitionEntity update = ServiceDefinitionEntity
				.builder()
				.serviceId(dbEntity.getServiceId())
				.basePath(dbEntity.getBasePath())
				.protocol(dbEntity.getProtocol())
				.enable(dbEntity.getEnable())
				.invokerMap(dbEntity.getInvokerMap())
				.draft("")
				.build();
		serviceDefinitionMapper.update(update);
		registerServiceDefinition(entityToModel(update));
	}

	public List<ServiceDefinition> getServiceDefinitionByDb() {
		List<ServiceDefinition> result = Lists.newArrayList();
		List<ServiceDefinitionEntity> definitionEntityList = serviceDefinitionMapper.selectAll();
		for (ServiceDefinitionEntity definition : definitionEntityList) {
			result.add(entityToModel(definition));
		}
		return result;
	}



	public void deleteServiceDefinitionByDb(String serviceId) {
		serviceDefinitionMapper.delete(serviceId);
	}


	private ServiceDefinition entityToModel(ServiceDefinitionEntity entity) {
		ServiceDefinition serviceDefinition = new ServiceDefinition();
		serviceDefinition.setServiceId(entity.getServiceId());
		serviceDefinition.setBasePath(entity.getBasePath());
		serviceDefinition.setProtocol(entity.getProtocol());
		serviceDefinition.setEnable(entity.getEnable());


		Map<String, ServiceInvoker> invokerMap = new HashMap<>();
		switch (entity.getProtocol()) {
			case GatewayProtocol.HTTP:
				Map<String, HttpServiceInvoker> httpInvokerMap = JSONUtil.parse(entity.getInvokerMap(), new TypeReference<Map<String, HttpServiceInvoker>>() {});
				invokerMap.putAll(httpInvokerMap);
				break;
			case GatewayProtocol.DUBBO:
				Map<String, DubboServiceInvoker> doubleInvokerMap = JSONUtil.parse(entity.getInvokerMap(), new TypeReference<Map<String, DubboServiceInvoker>>() {});
				invokerMap.putAll(doubleInvokerMap);
				break;
			default:
				break;
		}
		serviceDefinition.setInvokerMap(invokerMap);

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


	/**
	 * 注册服务定义对象
	 */
	protected void registerServiceDefinition(ServiceDefinition serviceDefinition) throws Exception {
		String key = namespace
				+ Registry.PATH
				+ serviceDefinition.getServiceId();
		if(!registryService.isExistKey(key)) {
			String value = FastJsonConvertUtil.convertObjectToJSON(serviceDefinition);
			registryService.registerPathIfNotExists(key, value, true);
		}
	}

	/**
	 * 为ServiceInvoker绑定一个规则ID
	 */
	public void serviceInvokerBindingRuleIdToDb(String serviceId, String invokerPath, Integer ruleId) throws Exception {
		// 更新db
		ServiceDefinitionEntity dbEntity = serviceDefinitionMapper.selectById(serviceId);
		ServiceDefinition serviceDefinition = entityToModel(dbEntity);
		serviceDefinition.getInvokerMap().get(invokerPath).setRuleId(ruleId);
		serviceDefinitionMapper.update(modelToEntity(serviceDefinition));
		// 更新缓存
		serviceInvokerBindingRuleId(serviceId, invokerPath, ruleId);
	}

	/**
	 * 为ServiceInvoker绑定一个规则ID
	 */
	public void serviceInvokerBindingRuleId(String serviceId, String invokerPath, Integer ruleId) throws Exception {
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
