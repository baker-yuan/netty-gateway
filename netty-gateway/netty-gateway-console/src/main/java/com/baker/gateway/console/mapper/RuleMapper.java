package com.baker.gateway.console.mapper;

import com.baker.gateway.console.entity.RuleEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleMapper {
    void insert(RuleEntity ruleEntity);
    Integer update(RuleEntity ruleEntity);
    void delete(String id);
    RuleEntity selectById(String id);
    List<RuleEntity> selectAll();
}
