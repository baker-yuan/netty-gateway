package com.baker.gateway.console.dto;

import java.util.HashSet;
import java.util.Set;

import com.baker.gateway.common.config.Rule;
import com.baker.gateway.common.config.Rule.FilterConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 规则
 */
public class RuleDTO {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AddOrUpdateRuleDTO {
		/**
		 * 前缀
		 */
		@NotBlank
		private String prefixPath;

		/**
		 * 规则ID
		 */
		@NotBlank
		private String id;

		/**
		 * 规则名称
		 */
		@NotBlank
		private String name;

		/**
		 * 服务唯一Id
		 */
		private String serviceId;

		/**
		 * route对应的协议
		 */
		@NotBlank
		private String protocol;

		/**
		 * 规则排序
		 */
		@NotNull
		@Min(1)
		private Integer order;

		/**
		 * 规则集合
		 */
		private Set<Rule.FilterConfig> filterConfigs = new HashSet<>();
	}


	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DeleteRuleDTO {
		/**
		 * 前缀
		 */
		@NotBlank
		private String prefixPath;

		/**
		 * 规则ID
		 */
		@NotBlank
		private String id;
	}


}
