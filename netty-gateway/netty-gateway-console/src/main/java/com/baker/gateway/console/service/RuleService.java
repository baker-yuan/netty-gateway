package com.baker.gateway.console.service;

import java.util.ArrayList;
import java.util.List;

import com.baker.gateway.common.util.JSONUtil;
import com.baker.gateway.console.entity.RuleEntity;
import com.baker.gateway.console.mapper.RuleMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baker.gateway.common.config.Rule;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.baker.gateway.common.util.Pair;
import com.baker.gateway.discovery.api.RegistryService;


@Service
public class RuleService {


	@Value("${gateway.console.namespace}")
	private String namespace;

	/**
	 * 注册服务接口
	 */
	@Autowired
	private RegistryService registryService;

	@Autowired
	private RuleMapper ruleMapper;


	/**
	 * 获取所有的规则列表
	 */
	public List<Rule> getRuleListByDb() throws Exception {
		List<RuleEntity> ruleEntityList = ruleMapper.selectAll();
		List<Rule> result = Lists.newArrayListWithCapacity(ruleEntityList.size());
		for (RuleEntity ruleEntity : ruleEntityList) {
			result.add(entityToModel(ruleEntity));
		}
		return result;
	}

    /**
     * 添加规则
     */
	public void addOrUpdateToDb(Rule rule) throws Exception {
		if (rule.getId() == null) {
			ruleMapper.insert(modelToEntity(rule));
		} else {
			ruleMapper.update(modelToEntity(rule));
		}
	}

	public void deleteRuleToDb(String id) {
		ruleMapper.delete(id);
	}

	private Rule entityToModel(RuleEntity ruleEntity) {
		Rule rule = new Rule();
		rule.setId(ruleEntity.getId());
		rule.setName(ruleEntity.getName());
		rule.setOrder(ruleEntity.getOrder());
		rule.setFilterConfigs(Sets.newHashSet(JSONUtil.parseToList(ruleEntity.getFilterConfigs(), Rule.FilterConfig.class)));
		return rule;
	}

	private RuleEntity modelToEntity(Rule rule) {
		RuleEntity ruleEntity = new RuleEntity();
		ruleEntity.setId(rule.getId());
		ruleEntity.setName(rule.getName());
		ruleEntity.setOrder(rule.getOrder());
		ruleEntity.setFilterConfigs(JSONUtil.toJSONString(rule.getFilterConfigs()));
		return ruleEntity;
	}


	/** ------------------------------- -------------------------------**/


	/**
	 * 获取所有的规则列表
	 */
	public List<Rule> getRuleList() throws Exception {
		String path = RegistryService.PATH 
				+ namespace
				+ RegistryService.RULE_PREFIX;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		List<Rule> rules = new ArrayList<>();
		for(Pair<String, String> pair : list) {
			String p = pair.getKey();
			if (p.equals(path)) { 
				continue;
			}
			String json = pair.getValue();
			Rule rule = FastJsonConvertUtil.convertJSONToObject(json, Rule.class);
			rules.add(rule);
		}
		return rules;
	}


	public void addRule(Rule rule) throws Exception {
		String path = RegistryService.PATH 
				+ namespace
				+ RegistryService.RULE_PREFIX;	
		String key = path + RegistryService.PATH + rule.getId();
		String value = FastJsonConvertUtil.convertObjectToJSON(rule);
		registryService.registerPersistentNode(key, value);	
	}
	

	public void updateRule(Rule rule) throws Exception {
		String path = RegistryService.PATH 
				+ namespace
				+ RegistryService.RULE_PREFIX;	
		String key = path + RegistryService.PATH + rule.getId();
		String value = FastJsonConvertUtil.convertObjectToJSON(rule);
		registryService.registerPersistentNode(key, value);
	}


	public void deleteRule(String ruleId) {
		String path = RegistryService.PATH 
				+ namespace
				+ RegistryService.RULE_PREFIX;	
		String key = path + RegistryService.PATH + ruleId;
		registryService.deleteByKey(key);
	}


}
