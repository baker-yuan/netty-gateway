package com.baker.gateway.console;

import java.util.ServiceLoader;

import com.baker.gateway.console.init.InitEtcdDir;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baker.gateway.discovery.api.RegistryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(GatewayConsoleProperties.class)
@ConditionalOnProperty(prefix = GatewayConsoleProperties.GATEWAY_CONSOLE_PREFIX, name = {"registryAddress", "namespace"})
public class MainConfig {

	@Bean
	public RegistryService registryService(GatewayConsoleProperties gatewayProperties) {
		ServiceLoader<RegistryService> serviceLoader = ServiceLoader.load(RegistryService.class);
		for(RegistryService registryService : serviceLoader) {
			registryService.initialized(gatewayProperties.getRegistryAddress());
			return registryService;
		}
		return null;
	}
	
	@Bean
	public ConsumerContainer consumerContainer(GatewayConsoleProperties gatewayProperties) {
		String kafkaAddress = gatewayProperties.getKafkaAddress();
		String groupId = gatewayProperties.getGroupId();
		String topicNamePrefix = gatewayProperties.getTopicNamePrefix();
		if(StringUtils.isBlank(kafkaAddress)) {
			log.warn("#MainConfig.consumerContainer# kafkaAddress is null, kafkaAddress = {}", kafkaAddress);
			return null;
		}
		if(StringUtils.isBlank(groupId)) {
			log.warn("#MainConfig.consumerContainer# groupId is null, groupId = {}", groupId);
			return null;
		}
		if(StringUtils.isBlank(topicNamePrefix)) {
			log.warn("#MainConfig.consumerContainer# topicNamePrefix is null, topicNamePrefix = {}", topicNamePrefix);
			return null;
		}
		return consumerContainer(gatewayProperties);
	}

	@Bean
	InitEtcdDir initEtcdDir(GatewayConsoleProperties properties) throws Exception {
		return new InitEtcdDir(properties);
	}
	
}
