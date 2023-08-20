package com.baker.gateway.console.web;

import java.util.List;

import com.baker.gateway.console.dto.ServiceDefinitionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baker.gateway.common.config.ServiceDefinition;
import com.baker.gateway.console.service.ServiceDefinitionService;

/**
 * 服务定义控制层
 */
@RestController
public class ServiceDefinitionController {

	@Autowired
	private ServiceDefinitionService serviceDefinitionService;
	
	/**
	 * 获取服务定义列表
	 *
	 * @param namespace 前缀路径
	 */
	@GetMapping("/serviceDefinition/getList")
	public List<ServiceDefinition> getList(@RequestParam("namespace") String namespace) throws Exception {
		return serviceDefinitionService.getServiceDefinitionList(namespace);
	}
	
	/**
	 * 根据serviceId更新服务定义PatternPath信息
	 */
	@RequestMapping("/serviceDefinition/updatePatternPathByServiceId")
	public void updatePatternPathByServiceId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
		if(serviceDefinitionDTO != null && serviceDefinitionDTO.getPatternPath()!= null) {
			serviceDefinitionService.updatePatternPathByServiceId(
					serviceDefinitionDTO.getNamespace(),
					serviceDefinitionDTO.getServiceId(),
					serviceDefinitionDTO.getPatternPath());			
		}
	}
	
	/**
	 * 根据serviceId更新服务定义PatternPath信息
	 */
	@RequestMapping("/serviceDefinition/updateEnableByServiceId")
	public void updateEnableByServiceId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
		if(serviceDefinitionDTO != null) {
			serviceDefinitionService.updateEnableByServiceId(
					serviceDefinitionDTO.getNamespace(),
					serviceDefinitionDTO.getServiceId(),
					serviceDefinitionDTO.isEnable());			
		}
	}
	
}
