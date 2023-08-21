package com.baker.gateway.console.web;

import java.util.List;

import com.baker.gateway.common.config.Rule;
import com.baker.gateway.console.dto.RuleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.console.dto.ServiceInstanceDTO;
import com.baker.gateway.console.service.ServiceInstanceService;

/**
 * 服务实例控制层
 */
@RestController
public class ServiceInstanceController {

	@Autowired
	private ServiceInstanceService serviceInstanceService;


	@PostMapping("/serviceInstance/addOrUpdate")
	public void addRule(@RequestBody @Validated ServiceInstanceDTO.AddOrUpdateServiceInstanceDTO req) throws Exception {
		ServiceInstance serviceInstance = new ServiceInstance();
		serviceInstance.setServiceInstanceId(req.getServiceInstanceId());
		serviceInstance.setServiceId(req.getServiceId());
		serviceInstanceService.addOrUpdate(serviceInstance);
	}



//	/**
//	 * 根据服务唯一ID获取服务实例列表
//	 */
//	@RequestMapping("/serviceInstance/getList")
//	public List<ServiceInstance> getList(@RequestParam("prefixPath") String prefixPath,
//			@RequestParam("serviceId")String serviceId) throws Exception{
//        return serviceInstanceService.getServiceInstanceList(prefixPath, serviceId);
//	}
//
//	/**
//	 * 启用禁用某个服务实例
//	 */
//	@RequestMapping("/serviceInstance/updateEnable")
//	public void updateEnable(@RequestBody ServiceInstanceDTO serviceInstanceDTO) throws Exception {
//		if(serviceInstanceDTO != null) {
//			serviceInstanceService.updateEnable(
//					serviceInstanceDTO.getNamespace(),
//					serviceInstanceDTO.getServiceId(),
//					serviceInstanceDTO.getServiceInstanceId(),
//					serviceInstanceDTO.isEnable());
//		}
//	}
//
//	/**
//	 * 对某个服务实例进行打标签
//	 */
//	@RequestMapping("/serviceInstance/updateTags")
//	public void updateTags(@RequestBody ServiceInstanceDTO serviceInstanceDTO) throws Exception {
//		if(serviceInstanceDTO != null) {
//			serviceInstanceService.updateTags(
//					serviceInstanceDTO.getNamespace(),
//					serviceInstanceDTO.getServiceId(),
//					serviceInstanceDTO.getServiceInstanceId(),
//					serviceInstanceDTO.getTags());
//		}
//	}
//
//	/**
//	 * 更新某个服务实例的权重
//	 */
//	@RequestMapping("/serviceInstance/updateWeight")
//	public void updateWeight(@RequestBody ServiceInstanceDTO serviceInstanceDTO) throws Exception {
//		if(serviceInstanceDTO != null) {
//			serviceInstanceService.updateWeight(
//					serviceInstanceDTO.getNamespace(),
//					serviceInstanceDTO.getServiceId(),
//					serviceInstanceDTO.getServiceInstanceId(),
//					serviceInstanceDTO.getWeight());
//		}
//	}

}
