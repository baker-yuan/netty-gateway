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
	 * 客户端上报
	 */
	@PostMapping("/serviceDefinition/report")
	public void addOrUpdateServiceDefinitionToDb(@RequestBody @Validated ServiceDefinition req) throws Exception {
		serviceDefinitionService.report(req);
	}

	/**
	 * 发布
	 */
	@PutMapping("/serviceDefinition/publish")
	public void addOrUpdateServiceDefinitionToDb(@RequestBody @Validated ServiceDefinitionDTO.PublishDTO req) throws Exception {
		serviceDefinitionService.publish(req.getServiceId());
	}

	/**
	 * 获取服务定义列表
	 */
	@GetMapping("/serviceDefinition/getList")
	public List<ServiceDefinition> getList() throws Exception {
		return serviceDefinitionService.getServiceDefinitionByDb();
	}


	@PostMapping("/serviceDefinition/delete")
	public void deleteServiceDefinition(@RequestBody @Validated ServiceDefinitionDTO.DeleteServiceDefinitionDTO req) {
		serviceDefinitionService.deleteServiceDefinitionByDb(req.getServiceId());
	}

}
