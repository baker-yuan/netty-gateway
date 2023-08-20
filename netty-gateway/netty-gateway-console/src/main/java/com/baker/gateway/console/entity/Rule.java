package com.baker.gateway.console.entity;

import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 规则模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule  {
    /**
     * 规则ID(全局唯一)
     */
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则排序，用于以后万一有需求做一个路径绑定多种规则，但是只能最终执行一个规则（按照该属性做优先级判断）
     */
    private Integer order;

    /**
     * 规则集合定义
     */
    private Set<FilterConfig> filterConfigs = new HashSet<>();

    /**
     * 过滤器的配置类
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class FilterConfig {
		/**
		 * 过滤器的唯一ID
		 */
		private String id;

		/**
		 * 过滤器的配置信息描述，json string  {timeout: 500}  {balance: rr}
		 */
		private String config;
    }

}
