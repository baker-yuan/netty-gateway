package com.baker.gateway.client.support.springmvc;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.baker.gateway.client.core.AbstractClientRegisterManager;
import com.baker.gateway.client.core.GatewayAnnotationScanner;
import com.baker.gateway.client.core.autoconfigure.GatewayProperties;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.baker.gateway.common.config.ServiceDefinition;
import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.common.constants.BasicConst;
import com.baker.gateway.common.constants.GatewayConst;
import com.baker.gateway.common.util.NetUtils;
import com.baker.gateway.common.util.TimeUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Http请求的客户端注册管理器
 */
@Slf4j
public class SpringMVCClientRegisterManager extends AbstractClientRegisterManager implements ApplicationListener<ApplicationEvent>, ApplicationContextAware  {

	private ApplicationContext applicationContext;

	@Autowired
	private ServerProperties serverProperties;
	
	private static final Set<Object> uniqueBeanSet = new HashSet<>();
	
	public SpringMVCClientRegisterManager(GatewayProperties gatewayProperties) throws Exception {
		super(gatewayProperties);
	}
	
	@PostConstruct
	private void init() {
		if(!ObjectUtils.allNotNull(serverProperties, serverProperties.getPort())) {
			return;
		}
		//	判断如果当前验证属性都为空 就进行初始化
		whetherStart = true;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(!whetherStart) {
			return;
		}
		
		if(event instanceof WebServerInitializedEvent ||
				event instanceof ServletWebServerInitializedEvent) {
			try {
				registerSpringMVC();
			} catch (Exception e) {
				log.error("#SpringMVCClientRegisteryManager# registerSpringMVC error", e);
			}
		} else if(event instanceof ApplicationStartedEvent){
			//	START:::
			System.err.println("******************************************");
			System.err.println("**        Gateway SpringMVC Started     **");
			System.err.println("******************************************");
		}
	}

	/**
	 * 解析SpringMvc的事件，进行注册
	 */
	private void registerSpringMVC() throws Exception {
		Map<String, RequestMappingHandlerMapping> allRequestMappings = BeanFactoryUtils
				.beansOfTypeIncludingAncestors(applicationContext, 
						RequestMappingHandlerMapping.class,
						true,
						false);
		
		for(RequestMappingHandlerMapping handlerMapping : allRequestMappings.values()) {
			Map<RequestMappingInfo, HandlerMethod> map = handlerMapping.getHandlerMethods();
			for(Map.Entry<RequestMappingInfo, HandlerMethod> me : map.entrySet()) {
				HandlerMethod handlerMethod = me.getValue();
				Class<?> clazz = handlerMethod.getBeanType();
				Object bean = applicationContext.getBean(clazz);
				//	如果当前Bean对象已经加载则不需要做任何事
				if(uniqueBeanSet.add(bean)) {
					ServiceDefinition serviceDefinition = GatewayAnnotationScanner.getInstance().scanbuilder(bean);
					if(serviceDefinition != null) {
						//	设置环境
						serviceDefinition.setEnvType(getEnv());
						//	注册服务定义
						registerServiceDefinition(serviceDefinition);
						
						//	注册服务实例
						ServiceInstance serviceInstance = new ServiceInstance();
						String localIp = NetUtils.getLocalIp();
						int port = serverProperties.getPort();
						String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
						String address = serviceInstanceId;
						String serviceId = serviceDefinition.getServiceId();
						String version = serviceDefinition.getVersion();
						
						serviceInstance.setServiceInstanceId(serviceInstanceId);
						serviceInstance.setServiceId(serviceId);
						serviceInstance.setAddress(address);
						serviceInstance.setWeight(GatewayConst.DEFAULT_WEIGHT);
						serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
						serviceInstance.setVersion(version);
						
						registerServiceInstance(serviceInstance);
					}
				}
			}
		}
	}
	
}
