package com.baker.gateway.console.mapper;

import com.baker.gateway.console.entity.ServiceDefinitionEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ServiceDefinitionMapper {
    void insert(ServiceDefinitionEntity serviceDefinitionEntity);
    void update(ServiceDefinitionEntity serviceDefinitionEntity);
    void delete(String serviceId);
    ServiceDefinitionEntity selectById(String serviceId);
    List<ServiceDefinitionEntity> selectAll();
}
