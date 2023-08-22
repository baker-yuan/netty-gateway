package com.baker.gateway.console.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.console.service.ServiceInstanceService;

/**
 * 服务实例控制层
 */
@RestController
public class ServiceInstanceController {

	@Autowired
	private ServiceInstanceService serviceInstanceService;

	/**
	 * 上报
	 */
	@PostMapping("/serviceInstance/report")
	public void report(@RequestBody @Validated ServiceInstance req) throws Exception {
		serviceInstanceService.report(req);
	}

}
