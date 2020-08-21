package com.inspur.apigateway.dao;

import com.inspur.apigateway.data.ServiceOutput;
import org.apache.ibatis.annotations.Mapper;
import org.loushang.framework.mybatis.mapper.EntityMapper;

import java.util.List;

@Mapper
public interface ServiceOutputMapper extends EntityMapper<ServiceOutput> {
    void deleteByApiId(String apiId);
    List<ServiceOutput> selectByApiId(String apiI);
}
