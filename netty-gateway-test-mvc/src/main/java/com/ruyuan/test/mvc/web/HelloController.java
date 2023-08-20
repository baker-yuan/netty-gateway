package com.ruyuan.test.mvc.web;

import com.baker.gateway.client.GatewayInvoker;
import com.baker.gateway.client.GatewayProtocol;
import com.baker.gateway.client.GatewayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.ruyuan.test.mvc.entity.TestEntity;

@RestController
@GatewayService(basePath = "/testMvc", protocol = GatewayProtocol.HTTP, serviceId = "hello")
public class HelloController {

	private volatile int count;
	
	@GatewayInvoker(path = "/testMvc/testGet")
    @GetMapping("/testMvc/testGet")
    public String testGet() {
        return "testGet";
    }
    
	@GatewayInvoker(path = "/testMvc/testPost")
    @PostMapping("/testMvc/testPost")
    public String testPost() {
		count++;
		if(count >= 1e5) {
			System.err.println("<------ baker: ------>");
			count = 0;
		}        
		return "baker";
    }
    
	@GatewayInvoker(path = "/testMvc/testParam")
    @RequestMapping("/testMvc/testParam")
    public String testParam(@RequestParam String name) {
		count++;
		if(count >= 1e5) {
			System.err.println("<------ testParam收到请求, name:" + name + " ------>");
			count = 0;
		}
    	return name;
    }
    
	@GatewayInvoker(path = "/testMvc/testEntity")
    @RequestMapping("/testMvc/testEntity")
    public String testEntity(@RequestBody TestEntity testEntity) {
        String result = "testEntity result :" + testEntity.getName() + testEntity.getAge();
        return result;
    }

}
