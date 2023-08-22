package com.baker.gateway.console.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baker.gateway.console.service.ServiceDefinitionService;


/**
 * 服务定义方法控制层
 */
@RestController
public class ServiceInvokerController {

	@Autowired
	private ServiceDefinitionService serviceDefinitionService;

	/**
	 * 为ServiceInvoker绑定一个规则ID
	 *
	 * @param serviceId 服务Id
	 * @param invokerPath 接口路径
	 * @param ruleId 规则id
	 */
	@RequestMapping("/serviceInvoker/bindingRuleId")
	public void bindingRuleId(@RequestParam("serviceId")String serviceId,
			@RequestParam("invokerPath")String invokerPath,
			@RequestParam("ruleId")Integer ruleId) throws Exception {
		serviceDefinitionService.serviceInvokerBindingRuleIdToDb(serviceId, invokerPath, ruleId);
	}
	
}
