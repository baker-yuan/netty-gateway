package com.baker.gateway.common.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
@JsonDeserialize(using = ServiceDefinitionDeserializer.class)
public class ServiceDefinition implements Serializable {

	private static final long serialVersionUID = -8263365765897285189L;

	/**
	 * 服务唯一id
	 */
	private String serviceId;

	/**
	 * 服务URL前缀，全局唯一
	 */
	private String basePath;
	
	/**
	 * 服务的版本号
	 */
	private String version;
	
	/**
	 * 服务的具体协议 http、dubbo、grpc
	 */
	private String protocol;

	/**
	 * 服务启用禁用
	 */
	private Boolean enable = true;
	
	/**
	 * 服务方法信息 key=invokerPath(完整路径) value=服务调用的接口模型描述
	 */
	private Map<String, ServiceInvoker> invokerMap;



	public ServiceDefinition(String serviceId, String version, String protocol,
			boolean enable, Map<String, ServiceInvoker> invokerMap) {
		this.serviceId = serviceId;
		this.version = version;
		this.protocol = protocol;
		this.enable = enable;
		this.invokerMap = invokerMap;
	}



}
