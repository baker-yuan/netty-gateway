package com.baker.gateway.console.dto;

import java.util.HashSet;
import java.util.Set;

import com.baker.gateway.common.config.Rule;
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
	public static class PublishDTO  {
		/**
		 * 规则ID
		 */
		private Integer id;

	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AddOrUpdateRuleDTO {
		/**
		 * 规则ID
		 */
		private Integer id;

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
		 * 规则ID
		 */
		@NotBlank
		private String id;
	}


}
