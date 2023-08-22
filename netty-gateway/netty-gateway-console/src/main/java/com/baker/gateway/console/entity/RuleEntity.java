package com.baker.gateway.console.entity;

import lombok.*;

/**
 * 规则模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEntity {
    /**
     * 规则ID(全局唯一)
     */
    private Integer id;

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
    private String filterConfigs;

    /**
     * 草稿
     */
    private String draft;
}
