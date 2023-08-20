package com.baker.gateway.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.baker.gateway.common.config.Rule;
import com.baker.gateway.common.config.Rule.FilterConfig;
import com.baker.gateway.common.constants.ProcessorFilterConstants;
import com.baker.gateway.common.constants.GatewayProtocol;
import com.baker.gateway.common.util.FastJsonConvertUtil;

public class RuleTest {

	@Test
	public void testHttpRule1() {
		Rule rule = new Rule();
		rule.setId("1");
		rule.setName("测试规则1");
		rule.setOrder(1);
		rule.setProtocol(GatewayProtocol.HTTP);
		
		Set<FilterConfig> filterConfigs = new HashSet<>();
		
		//	负载均衡过滤器
		Rule.FilterConfig fc1 = new Rule.FilterConfig();
		fc1.setId(ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_ID);

		Map<String, String> param1 = new HashMap<>();
		param1.put("loadBalanceStrategy", "RANDOM");
		param1.put("loggable", "false");
		String configStr1 = FastJsonConvertUtil.convertObjectToJSON(param1);
		fc1.setConfig(configStr1);
		filterConfigs.add(fc1);
		
		Rule.FilterConfig fc2 = new Rule.FilterConfig();
		fc2.setId(ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ID);
		Map<String, String> param2 = new HashMap<>();
		param2.put("timeout", "4000");
		param2.put("loggable", "false");
		String configStr2 = FastJsonConvertUtil.convertObjectToJSON(param2);
		fc2.setConfig(configStr2);
		filterConfigs.add(fc2);
		
		Rule.FilterConfig fc3 = new Rule.FilterConfig();
		fc3.setId(ProcessorFilterConstants.HTTP_ROUTE_FILTER_ID);
		Map<String, String> param3 = new HashMap<>();
		param3.put("loggable", "false");
		String configStr3 = FastJsonConvertUtil.convertObjectToJSON(param3);
		fc3.setConfig(configStr3);
		filterConfigs.add(fc3);
		
		Rule.FilterConfig fc4 = new Rule.FilterConfig();
		fc4.setId(ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID);
		Map<String, String> param4 = new HashMap<>();
		param4.put("loggable", "false");
		String configStr4 = FastJsonConvertUtil.convertObjectToJSON(param4);
		fc4.setConfig(configStr4);
		filterConfigs.add(fc4);
		
		rule.setFilterConfigs(filterConfigs);
		
		String parse = FastJsonConvertUtil.convertObjectToJSON(rule);
		System.err.println(parse);
	
	}
	
	@Test
	public void testDubboRule2() {
		
		Rule rule = new Rule();
		rule.setId("2");
		rule.setName("测试规则2");
		rule.setOrder(2);
		rule.setProtocol(GatewayProtocol.DUBBO);
		
		Set<FilterConfig> filterConfigs = new HashSet<>();
		
		//	负载均衡过滤器
		Rule.FilterConfig fc1 = new Rule.FilterConfig();
		fc1.setId(ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_ID);

		Map<String, String> param1 = new HashMap<>();
		param1.put("loadBalanceStrategy", "RANDOM");
		param1.put("loggable", "false");
		String configStr1 = FastJsonConvertUtil.convertObjectToJSON(param1);
		fc1.setConfig(configStr1);
		filterConfigs.add(fc1);
		
		Rule.FilterConfig fc2 = new Rule.FilterConfig();
		fc2.setId(ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ID);
		Map<String, String> param2 = new HashMap<>();
		param2.put("timeout", "6000");
		param2.put("loggable", "false");
		String configStr2 = FastJsonConvertUtil.convertObjectToJSON(param2);
		fc2.setConfig(configStr2);
		filterConfigs.add(fc2);
		
		Rule.FilterConfig fc3 = new Rule.FilterConfig();
		fc3.setId(ProcessorFilterConstants.DUBBO_ROUTE_FILTER_ID);
		Map<String, String> param3 = new HashMap<>();
		param3.put("loggable", "false");
		String configStr3 = FastJsonConvertUtil.convertObjectToJSON(param3);
		fc3.setConfig(configStr3);
		filterConfigs.add(fc3);
		
		Rule.FilterConfig fc4 = new Rule.FilterConfig();
		fc4.setId(ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID);
		Map<String, String> param4 = new HashMap<>();
		param4.put("loggable", "false");
		String configStr4 = FastJsonConvertUtil.convertObjectToJSON(param4);
		fc4.setConfig(configStr4);
		filterConfigs.add(fc4);
		
		rule.setFilterConfigs(filterConfigs);
		
		String parse = FastJsonConvertUtil.convertObjectToJSON(rule);
		System.err.println(parse);

	}
	
	@Test
	public void testHttpRule3() {
		Rule rule = new Rule();
		rule.setId("1");
		rule.setName("测试规则1");
		rule.setOrder(1);
		rule.setProtocol(GatewayProtocol.HTTP);
		
		Set<FilterConfig> filterConfigs = new HashSet<>();
		
		//	负载均衡过滤器
		Rule.FilterConfig fc1 = new Rule.FilterConfig();
		fc1.setId(ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_ID);

		Map<String, String> param1 = new HashMap<>();
		param1.put("loadBalanceStrategy", "RANDOM");
		param1.put("loggable", "false");
		String configStr1 = FastJsonConvertUtil.convertObjectToJSON(param1);
		fc1.setConfig(configStr1);
		filterConfigs.add(fc1);
		
		Rule.FilterConfig fc2 = new Rule.FilterConfig();
		fc2.setId(ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ID);
		Map<String, String> param2 = new HashMap<>();
		param2.put("timeout", "4000");
		param2.put("loggable", "false");
		String configStr2 = FastJsonConvertUtil.convertObjectToJSON(param2);
		fc2.setConfig(configStr2);
		filterConfigs.add(fc2);
		
		Rule.FilterConfig fc3 = new Rule.FilterConfig();
		fc3.setId(ProcessorFilterConstants.HTTP_ROUTE_FILTER_ID);
		Map<String, String> param3 = new HashMap<>();
		param3.put("loggable", "false");
		String configStr3 = FastJsonConvertUtil.convertObjectToJSON(param3);
		fc3.setConfig(configStr3);
		filterConfigs.add(fc3);
		
		Rule.FilterConfig fc4 = new Rule.FilterConfig();
		fc4.setId(ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID);
		Map<String, String> param4 = new HashMap<>();
		param4.put("loggable", "false");
		String configStr4 = FastJsonConvertUtil.convertObjectToJSON(param4);
		fc4.setConfig(configStr4);
		filterConfigs.add(fc4);
		
		Rule.FilterConfig fc5 = new Rule.FilterConfig();
		fc5.setId(ProcessorFilterConstants.STATISTICS_POST_FILTER_ID);
		Map<String, String> param5 = new HashMap<>();
		param5.put("loggable", "false");
		param5.put("rollingNumber", "true");
		String configStr5 = FastJsonConvertUtil.convertObjectToJSON(param5);
		fc4.setConfig(configStr5);
		filterConfigs.add(fc5);
		
		rule.setFilterConfigs(filterConfigs);
		
		String parse = FastJsonConvertUtil.convertObjectToJSON(rule);
		System.err.println(parse);
	
	}
	
	@Test
	public void testDubboRule4() {
		
		Rule rule = new Rule();
		rule.setId("2");
		rule.setName("测试规则2");
		rule.setOrder(2);
		rule.setProtocol(GatewayProtocol.DUBBO);
		
		Set<FilterConfig> filterConfigs = new HashSet<>();
		
		//	负载均衡过滤器
		Rule.FilterConfig fc1 = new Rule.FilterConfig();
		fc1.setId(ProcessorFilterConstants.LOADBALANCE_PRE_FILTER_ID);

		Map<String, String> param1 = new HashMap<>();
		param1.put("loadBalanceStrategy", "RANDOM");
		param1.put("loggable", "false");
		String configStr1 = FastJsonConvertUtil.convertObjectToJSON(param1);
		fc1.setConfig(configStr1);
		filterConfigs.add(fc1);
		
		Rule.FilterConfig fc2 = new Rule.FilterConfig();
		fc2.setId(ProcessorFilterConstants.TIMEOUT_PRE_FILTER_ID);
		Map<String, String> param2 = new HashMap<>();
		param2.put("timeout", "6000");
		param2.put("loggable", "false");
		String configStr2 = FastJsonConvertUtil.convertObjectToJSON(param2);
		fc2.setConfig(configStr2);
		filterConfigs.add(fc2);
		
		Rule.FilterConfig fc3 = new Rule.FilterConfig();
		fc3.setId(ProcessorFilterConstants.DUBBO_ROUTE_FILTER_ID);
		Map<String, String> param3 = new HashMap<>();
		param3.put("loggable", "false");
		String configStr3 = FastJsonConvertUtil.convertObjectToJSON(param3);
		fc3.setConfig(configStr3);
		filterConfigs.add(fc3);
		
		Rule.FilterConfig fc4 = new Rule.FilterConfig();
		fc4.setId(ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID);
		Map<String, String> param4 = new HashMap<>();
		param4.put("loggable", "false");
		String configStr4 = FastJsonConvertUtil.convertObjectToJSON(param4);
		fc4.setConfig(configStr4);
		filterConfigs.add(fc4);
		
		Rule.FilterConfig fc5 = new Rule.FilterConfig();
		fc5.setId(ProcessorFilterConstants.STATISTICS_POST_FILTER_ID);
		Map<String, String> param5 = new HashMap<>();
		param5.put("loggable", "false");
		param5.put("rollingNumber", "true");
		String configStr5 = FastJsonConvertUtil.convertObjectToJSON(param5);
		fc4.setConfig(configStr5);
		filterConfigs.add(fc5);
		
		rule.setFilterConfigs(filterConfigs);
		
		String parse = FastJsonConvertUtil.convertObjectToJSON(rule);
		System.err.println(parse);

	}
	
	
}
