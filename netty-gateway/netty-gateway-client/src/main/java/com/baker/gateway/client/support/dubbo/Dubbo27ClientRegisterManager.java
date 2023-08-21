package com.baker.gateway.client.support.dubbo;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.baker.gateway.client.core.AbstractClientRegisterManager;
import com.baker.gateway.client.core.GatewayAnnotationScanner;
import com.baker.gateway.client.core.autoconfigure.GatewayProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.baker.gateway.common.config.ServiceDefinition;
import com.baker.gateway.common.config.ServiceInstance;
import com.baker.gateway.common.constants.BasicConst;
import com.baker.gateway.common.constants.GatewayConst;
import com.baker.gateway.common.util.NetUtils;
import com.baker.gateway.common.util.TimeUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * dubbo 2.7.x 客户端注册管理类实现
 */
@Slf4j
public class Dubbo27ClientRegisterManager extends AbstractClientRegisterManager implements EnvironmentAware, ApplicationListener<ApplicationEvent> {

	public Dubbo27ClientRegisterManager(GatewayProperties gatewayProperties) throws Exception {
		super(gatewayProperties);
	}
	
	private Environment environment;
	
	private static final Set<Object> uniqueBeanSet = new HashSet<>();

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
	@PostConstruct
	private void init() {
		String port = environment.getProperty(DubboConstants.DUBBO_PROTOCOL_PORT);
		if(StringUtils.isEmpty(port)) {
			log.error("Gateway Dubbo服务未启动");
			return;
		}
		whetherStart = true;
	}
	
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(!whetherStart) {
			return;
		}
		if(event instanceof ServiceBeanExportedEvent) {
			ServiceBean<?> serviceBean = ((ServiceBeanExportedEvent)event).getServiceBean();
			try {
				registerServiceBean(serviceBean);
			} catch (Exception e) {
				log.error("Gateway Dubbo 注册服务ServiceBean 失败，ServiceBean = {}", serviceBean, e);
			}
		} else if(event instanceof ApplicationStartedEvent){
			//	START:::
			System.err.println("******************************************");
			System.err.println("**        Gateway Dubbo Started         **");
			System.err.println("******************************************");
		}
	}

	/**
	 * 注册Dubbo服务：从ServiceBeanExportedEvent获取ServiceBean对象
	 */
	private void registerServiceBean(ServiceBean<?> serviceBean) throws Exception {
		Object bean = serviceBean.getRef();
		if(uniqueBeanSet.add(bean)) {
			ServiceDefinition serviceDefinition = GatewayAnnotationScanner.getInstance().scanbuilder(bean, serviceBean);
			if(serviceDefinition != null) {
				//	设置环境
//				serviceDefinition.setEnvType(getEnv());
				//	注册服务定义
				registerServiceDefinition(serviceDefinition);
				
				//	注册服务实例
				ServiceInstance serviceInstance = new ServiceInstance();
				String localIp = NetUtils.getLocalIp();
				int port = serviceBean.getProtocol().getPort();
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
