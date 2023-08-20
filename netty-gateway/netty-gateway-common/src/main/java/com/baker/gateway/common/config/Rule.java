package com.baker.gateway.common.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 规则模型
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Rule implements Comparable<Rule>, Serializable {

    private static final long serialVersionUID = 2540640682854847548L;

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
    private Set<Rule.FilterConfig> filterConfigs = new HashSet<>();


    public Rule(String id, String name, Integer order, Set<FilterConfig> filterConfigs) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.filterConfigs = filterConfigs;
    }

    /**
     * 向规则里面添加指定的过滤器
     */
    public boolean addFilterConfig(Rule.FilterConfig filterConfig) {
        return filterConfigs.add(filterConfig);
    }

    /**
     * 通过一个指定的filterId，获取getFilterConfig
     */
    public Rule.FilterConfig getFilterConfig(String id) {
        for (Rule.FilterConfig filterConfig : filterConfigs) {
            if (filterConfig.getId().equalsIgnoreCase(id)) {
                return filterConfig;
            }
        }
        return null;
    }

    /**
     * 根据传入的filterId，判断当前Rule中是否存在
     */
    public boolean hashId(String id) {
        for (Rule.FilterConfig filterConfig : filterConfigs) {
            if (filterConfig.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Rule o) {
        int orderCompare = Integer.compare(getOrder(), o.getOrder());
        if (orderCompare == 0) {
            return getId().compareTo(o.getId());
        }
        return orderCompare;
    }


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
