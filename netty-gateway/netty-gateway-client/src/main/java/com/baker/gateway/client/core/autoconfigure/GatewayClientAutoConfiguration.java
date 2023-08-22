package com.baker.gateway.client.core.autoconfigure;

import javax.servlet.Servlet;

import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.baker.gateway.client.support.dubbo.Dubbo27ClientRegisterManager;
import com.baker.gateway.client.support.springmvc.SpringMVCClientRegisterManager;

/**
 * SpringBoot自动装配加载类
 */
@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
@ConditionalOnProperty(prefix = GatewayProperties.GATEWAY_PREFIX, name = {"consoleUrl"})
public class GatewayClientAutoConfiguration {

	@Bean
	@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
	@ConditionalOnMissingBean(SpringMVCClientRegisterManager.class)
	public SpringMVCClientRegisterManager springMVCClientRegisterManager(GatewayProperties gatewayProperties) throws Exception {
		return new SpringMVCClientRegisterManager(gatewayProperties);
	}
	
	@Bean
	@ConditionalOnClass({ServiceBean.class})
	@ConditionalOnMissingBean(Dubbo27ClientRegisterManager.class)
	public Dubbo27ClientRegisterManager dubbo27ClientRegisterManager(GatewayProperties gatewayProperties) throws Exception {
		return new Dubbo27ClientRegisterManager(gatewayProperties);
	}
	
}
