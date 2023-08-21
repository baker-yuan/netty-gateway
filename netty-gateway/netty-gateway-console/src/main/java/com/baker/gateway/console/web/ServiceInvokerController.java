package com.baker.gateway.console.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baker.gateway.common.config.ServiceInvoker;
import com.baker.gateway.console.service.ServiceDefinitionService;


/**
 * 服务定义方法控制层
 */
@RestController
public class ServiceInvokerController {

	@Autowired
	private ServiceDefinitionService serviceDefinitionService;

	/**
	 * 根据serviceId获取指定的服务下的调用方法列表
	 *
	 * @param serviceId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/serviceInvoker/getListByServiceId")
	public List<ServiceInvoker> getListByServiceId(
			@RequestParam("serviceId")String serviceId) throws Exception{
        return serviceDefinitionService.getServiceInvokerByServiceId(serviceId);
	}

	/**
	 * 为ServiceInvoker绑定一个规则ID
	 *
	 * @param serviceId
	 * @param invokerPath
	 * @param ruleId
	 * @throws Exception
	 */
	@RequestMapping("/serviceInvoker/bindingRuleId")
	public void bindingRuleId(@RequestParam("serviceId")String serviceId,
			@RequestParam("invokerPath")String invokerPath,
			@RequestParam("ruleId")String ruleId) throws Exception {
		serviceDefinitionService.serviceInvokerBindingRuleId(serviceId, invokerPath, ruleId);
	}
	
}
