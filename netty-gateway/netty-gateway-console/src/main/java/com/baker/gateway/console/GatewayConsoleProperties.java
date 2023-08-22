package com.baker.gateway.console;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = GatewayConsoleProperties.GATEWAY_CONSOLE_PREFIX)
public class GatewayConsoleProperties {

	public static final String GATEWAY_CONSOLE_PREFIX = "gateway.console";
	
	private String registryAddress;

	private String namespace;


	private String kafkaAddress;
	
	private String groupId;
	
	private String topicNamePrefix;
	
	private int ConsumerNum = 1;

}
