package com.baker.gateway.client.core.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 配置类
 */
@Data
@ConfigurationProperties(prefix = GatewayProperties.GATEWAY_PREFIX)
public class GatewayProperties {

	public static final String GATEWAY_PREFIX = "gateway";
	
	/**
	 * 	etcd注册中心地址
	 */
	private String registryAddress;
	
	/**
	 * 	etcd注册命名空间
	 */
	private String namespace = GATEWAY_PREFIX;
	
	/**
	 * 	环境属性
	 */
	private String env = "dev";
	
	
}
