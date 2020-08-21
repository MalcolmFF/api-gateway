package com.inspur.apigateway.dao;

import com.inspur.apigateway.data.ServiceGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ServiceGroupMapper {
	
	void insert(ServiceGroup group);

	void delete(String groupId);

	void update(ServiceGroup group);

	ServiceGroup getById(String groupId);

	List<ServiceGroup> getGroupList(Map<String, Object> param);

	ServiceGroup isRegisted(String parentId, String name);
}
