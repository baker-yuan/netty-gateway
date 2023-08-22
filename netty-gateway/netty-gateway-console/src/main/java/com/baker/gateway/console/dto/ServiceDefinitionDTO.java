package com.baker.gateway.console.dto;

import com.baker.gateway.common.config.ServiceInvoker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 服务
 */
public class ServiceDefinitionDTO {


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PublishDTO  {
		/**
		 * 服务唯一id
		 */
		private String serviceId;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AddOrUpdateServiceDefinitionDTO  {
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
		private Boolean enable;

		/**
		 * 服务方法信息 key=invokerPath(完整路径) value=服务调用的接口模型描述
		 */
		private Map<String, ServiceInvoker> invokerMap;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DeleteServiceDefinitionDTO{
		/**
		 * 	服务唯一ID
		 */
		private String serviceId;
	}


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Update {
		/**
		 * 	服务唯一ID
		 */
		private String serviceId;

		/**
		 * 	访问真实ANT表达式匹配
		 */
		private String patternPath;

		/**
		 *	启用禁用服务
		 */
		private boolean enable = true;
	}
}
