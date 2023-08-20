package com.baker.gateway.console.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baker.gateway.common.config.Rule;
import com.baker.gateway.common.util.FastJsonConvertUtil;
import com.baker.gateway.common.util.Pair;
import com.baker.gateway.discovery.api.RegistryService;


@Service
public class RuleService {

	/**
	 * 注册服务接口
	 */
	@Autowired
	private RegistryService registryService;

	/**
	 * 获取所有的规则列表
	 */
	public List<Rule> getRuleList(String prefixPath) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
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


	public void addRule(String prefixPath, Rule rule) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.RULE_PREFIX;	
		String key = path + RegistryService.PATH + rule.getId();
		String value = FastJsonConvertUtil.convertObjectToJSON(rule);
		registryService.registerPersistentNode(key, value);	
	}
	

	public void updateRule(String prefixPath, Rule rule) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.RULE_PREFIX;	
		String key = path + RegistryService.PATH + rule.getId();
		String value = FastJsonConvertUtil.convertObjectToJSON(rule);
		registryService.registerPersistentNode(key, value);
	}


	public void deleteRule(String prefixPath, String ruleId) {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.RULE_PREFIX;	
		String key = path + RegistryService.PATH + ruleId;
		registryService.deleteByKey(key);
	}
	
}
