package com.baker.gateway.console.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务实例
 */

public class ServiceInstanceDTO {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AddOrUpdateServiceInstanceDTO {

		/**
		 * 	前缀
		 */
		private String namespace;
		/**
		 * 	服务唯一ID
		 */
		private String serviceId;

		/**
		 * 	服务实例ID = address
		 */
		private String serviceInstanceId;

		/**
		 *	启用禁用服务实例
		 */
		private boolean enable = true;

		/**
		 * 	路由标签
		 */
		private String tags;

		/**
		 * 	权重
		 */
		private Integer weight;
	}


}
