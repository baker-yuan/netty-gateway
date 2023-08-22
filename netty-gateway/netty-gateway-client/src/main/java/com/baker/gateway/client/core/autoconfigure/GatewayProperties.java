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
	 * 控制台地址
	 */
	private String consoleUrl;
}
