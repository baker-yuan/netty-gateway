package com.baker.gateway.console.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDefinitionDTO {
	
	/**
	 * 	前缀
	 */
	private String prefixPath;	// namespace = netty-gateway-dev
	/**
	 * 	服务唯一ID
	 */
	private String uniqueId;	//	serviceId:version

    /**
     * 	访问真实ANT表达式匹配
     */
    private String patternPath;

	/**
     *	启用禁用服务
     */
    private boolean enable = true;
}