package com.baker.gateway.common.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 资源服务定义类，无论下游是什么样的服务都需要进行注册
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ServiceDefinition implements Serializable {

	private static final long serialVersionUID = -8263365765897285189L;
	
	/**
	 * 服务ID(唯一) serviceId:version
	 */
	private String uniqueId;
	
	/**
	 * 服务唯一id
	 */
	private String serviceId;
	
	/**
	 * 服务的版本号
	 */
	private String version;
	
	/**
	 * 服务的具体协议 http、dubbo、grpc
	 */
	private String protocol;
	
	/**
	 * 路径匹配规则，访问真实ANT表达式，定义具体的服务路径的匹配规则
	 */
	private String patternPath;
	
	/**
	 * 环境名称
	 */
	private String envType;

	/**
	 * 服务启用禁用
	 */
	private boolean enable = true;
	
	/**
	 * 服务方法信息 key=invokerPath(完整路径) value=服务调用的接口模型描述
	 */
	private Map<String, ServiceInvoker> invokerMap;



	public ServiceDefinition(String uniqueId, String serviceId, String version, String protocol, String patternPath,
			String envType, boolean enable, Map<String, ServiceInvoker> invokerMap) {
		this.uniqueId = uniqueId;
		this.serviceId = serviceId;
		this.version = version;
		this.protocol = protocol;
		this.patternPath = patternPath;
		this.envType = envType;
		this.enable = enable;
		this.invokerMap = invokerMap;
	}

}
