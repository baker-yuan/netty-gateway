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
	 * @param prefixPath 前缀路径
	 */
	@GetMapping("/serviceDefinition/getList")
	public List<ServiceDefinition> getList(@RequestParam("prefixPath") String prefixPath) throws Exception {
		return serviceDefinitionService.getServiceDefinitionList(prefixPath);
	}
	
	/**
	 * 根据uniqueId更新服务定义PatternPath信息
	 */
	@RequestMapping("/serviceDefinition/updatePatternPathByUniqueId")
	public void updatePatternPathByUniqueId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
		if(serviceDefinitionDTO != null && serviceDefinitionDTO.getPatternPath()!= null) {
			serviceDefinitionService.updatePatternPathByUniqueId(
					serviceDefinitionDTO.getPrefixPath(),
					serviceDefinitionDTO.getUniqueId(),
					serviceDefinitionDTO.getPatternPath());			
		}
	}
	
	/**
	 * 根据uniqueId更新服务定义PatternPath信息
	 */
	@RequestMapping("/serviceDefinition/updateEnableByUniqueId")
	public void updateEnableByUniqueId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
		if(serviceDefinitionDTO != null) {
			serviceDefinitionService.updateEnableByUniqueId(
					serviceDefinitionDTO.getPrefixPath(),
					serviceDefinitionDTO.getUniqueId(),
					serviceDefinitionDTO.isEnable());			
		}
	}
	
}
