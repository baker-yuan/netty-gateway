package com.baker.gateway.console.web;

import java.util.List;

import com.baker.gateway.console.dto.ServiceDefinitionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
	 */
	@GetMapping("/serviceDefinition/getList")
	public List<ServiceDefinition> getList() throws Exception {
		return serviceDefinitionService.getServiceDefinitionByDb();
	}


	@PostMapping("/serviceDefinition/report")
	public void addOrUpdateServiceDefinitionToDb(@RequestBody @Validated ServiceDefinition req) throws Exception {
		serviceDefinitionService.report(req);
	}



	@PostMapping("/serviceDefinition/delete")
	public void deleteServiceDefinition(@RequestBody @Validated ServiceDefinitionDTO.DeleteServiceDefinitionDTO req) {
		serviceDefinitionService.deleteServiceDefinition(req.getServiceId());
	}











	
//	/**
//	 * 根据serviceId更新服务定义PatternPath信息
//	 */
//	@RequestMapping("/serviceDefinition/updatePatternPathByServiceId")
//	public void updatePatternPathByServiceId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
//		if(serviceDefinitionDTO != null && serviceDefinitionDTO.getPatternPath()!= null) {
//			serviceDefinitionService.updatePatternPathByServiceId(
//					serviceDefinitionDTO.getServiceId(),
//					serviceDefinitionDTO.getPatternPath());
//		}
//	}
//
//	/**
//	 * 根据serviceId更新服务定义PatternPath信息
//	 */
//	@RequestMapping("/serviceDefinition/updateEnableByServiceId")
//	public void updateEnableByServiceId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
//		if(serviceDefinitionDTO != null) {
//			serviceDefinitionService.updateEnableByServiceId(
//					serviceDefinitionDTO.getServiceId(),
//					serviceDefinitionDTO.isEnable());
//		}
//	}
	
}
