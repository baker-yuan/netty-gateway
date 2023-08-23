package com.baker.gateway.console.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import org.springframework.beans.BeanUtils;
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

		Map<String, ServiceInvoker> invokerMap = new HashMap<>();
		if (definitionDiff(dbEntity, updateEntity, invokerMap)) {
			return;
		}

		// 保存到草稿中，手动发布上线，只能修改指定字段
		ServiceDefinitionEntity draft = new ServiceDefinitionEntity();
		BeanUtils.copyProperties(dbEntity, draft);
		draft.setInvokerMap(JSONUtil.toJSONString(invokerMap));
		dbEntity.setDraft(JSONUtil.toJSONString(draft));
		serviceDefinitionMapper.update(dbEntity);
	}

	public boolean definitionDiff(ServiceDefinitionEntity dbEntity, ServiceDefinitionEntity updateEntity, Map<String, ServiceInvoker> invokerMap) {
		switch (dbEntity.getProtocol()) {
			case GatewayProtocol.HTTP:
				Map<String, HttpServiceInvoker> dbHttpInvokerMap = JSONUtil.parse(dbEntity.getInvokerMap(), new TypeReference<Map<String, HttpServiceInvoker>>() {});
				Map<String, HttpServiceInvoker> reportHttpInvokerMap = JSONUtil.parse(updateEntity.getInvokerMap(), new TypeReference<Map<String, HttpServiceInvoker>>() {});
				// 相等判断
				if (dbHttpInvokerMap.size() == reportHttpInvokerMap.size()) {
					boolean equals = true;
					// 遍历上报数据
					for (Map.Entry<String, HttpServiceInvoker> entry : reportHttpInvokerMap.entrySet()) {
						if (dbHttpInvokerMap.containsKey(entry.getKey())) {
							if (!dbHttpInvokerMap.get(entry.getKey()).bizEquals(entry.getValue())) {
								equals = false;
							}
						}
					}
					if (equals) {
						return true;
					}
				}
				// 不相等
				for (Map.Entry<String, HttpServiceInvoker> entry : reportHttpInvokerMap.entrySet()) {
					if (dbHttpInvokerMap.containsKey(entry.getKey())) {
						HttpServiceInvoker value = entry.getValue();
						value.setRuleId(dbHttpInvokerMap.get(entry.getKey()).getRuleId());
						invokerMap.put(entry.getKey(), value);
					} else {
						invokerMap.put(entry.getKey(), entry.getValue());
					}
				}
				break;
			case GatewayProtocol.DUBBO:
				Map<String, DubboServiceInvoker> dbDoubleInvokerMap = JSONUtil.parse(dbEntity.getInvokerMap(), new TypeReference<Map<String, DubboServiceInvoker>>() {});
				Map<String, DubboServiceInvoker> reportDoubleInvokerMap = JSONUtil.parse(updateEntity.getInvokerMap(), new TypeReference<Map<String, DubboServiceInvoker>>() {});
				// 相等判断
				if (dbDoubleInvokerMap.size() == reportDoubleInvokerMap.size()) {
					boolean equals = true;
					// 遍历上报数据
					for (Map.Entry<String, DubboServiceInvoker> entry : reportDoubleInvokerMap.entrySet()) {
						if (dbDoubleInvokerMap.containsKey(entry.getKey())) {
							if (!dbDoubleInvokerMap.get(entry.getKey()).bizEquals(entry.getValue())) {
								equals = false;
							}
						}
					}
					if (equals) {
						return true;
					}
				}
				// 不相等
				for (Map.Entry<String, DubboServiceInvoker> entry : reportDoubleInvokerMap.entrySet()) {
					if (dbDoubleInvokerMap.containsKey(entry.getKey())) {
						DubboServiceInvoker value = entry.getValue();
						value.setRuleId(dbDoubleInvokerMap.get(entry.getKey()).getRuleId());
						invokerMap.put(entry.getKey(), value);
					} else {
						invokerMap.put(entry.getKey(), entry.getValue());
					}
				}
				break;
			default:
				break;
		}
		return false;
	}

	/**
	 * 发布
	 */
	public void publish(String serviceId) throws Exception {
		ServiceDefinitionEntity dbEntity = serviceDefinitionMapper.selectById(serviceId);
		dbEntity.setDraft("");
		serviceDefinitionMapper.update(dbEntity);

		registerServiceDefinition(entityToModel(dbEntity));
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
		String key = Registry.PATH +
				namespace
				+ Registry.SERVICE_PREFIX
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
