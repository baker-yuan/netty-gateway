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


	@PostMapping("/serviceDefinition/addOrUpdate")
	public void addOrUpdateServiceDefinitionToDb(@RequestBody @Validated ServiceDefinitionDTO.AddOrUpdateServiceDefinitionDTO req) throws Exception {
		ServiceDefinition serviceDefinition = new ServiceDefinition();
		serviceDefinition.setServiceId(req.getServiceId());
		serviceDefinition.setBasePath(req.getBasePath());
		serviceDefinition.setVersion(req.getVersion());
		serviceDefinition.setProtocol(req.getProtocol());
		serviceDefinition.setEnvType(req.getEnvType());
		serviceDefinition.setEnable(req.getEnable());
		serviceDefinition.setInvokerMap(req.getInvokerMap());
		serviceDefinitionService.addOrUpdateServiceDefinitionToDb(serviceDefinition);
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
