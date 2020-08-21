package com.inspur.apigateway.dao;

import com.inspur.apigateway.data.ServiceInput;
import org.apache.ibatis.annotations.Mapper;
import org.loushang.framework.mybatis.mapper.EntityMapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ServiceInputMapper  extends EntityMapper<ServiceInput> {

	List<ServiceInput> listServiceInputs(Map map);

	void deleteByApiId(String id);
}
