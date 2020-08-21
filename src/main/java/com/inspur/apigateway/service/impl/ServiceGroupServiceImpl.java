package com.inspur.apigateway.service.impl;

import com.inspur.apigateway.dao.ServiceGroupMapper;
import com.inspur.apigateway.data.ServiceGroup;
import com.inspur.apigateway.service.IServiceGroupService;
import lombok.RequiredArgsConstructor;
import org.loushang.framework.util.UUIDGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("serviceGroupService")
@RequiredArgsConstructor
public class ServiceGroupServiceImpl implements IServiceGroupService {

	final private ServiceGroupMapper serviceGroupMapper;

	@Override
	public String insert(ServiceGroup group) {
		String groupId = UUIDGenerator.getUUID();
		group.setId(groupId);
		serviceGroupMapper.insert(group);
		return groupId;
	}

	@Override
	public void delete(String groupId) {
		serviceGroupMapper.delete(groupId);
	}

	@Override
	public void update(ServiceGroup group) {
		serviceGroupMapper.update(group);
	}

	@Override
	public ServiceGroup getById(String groupId) {
		return serviceGroupMapper.getById(groupId);
	}

	@Override
	public List<ServiceGroup> getGroupList(Map<String, Object> param) {
		return serviceGroupMapper.getGroupList(param);
	}
	
	@Override
	public boolean isRegisted(String parentId, String name) {
		ServiceGroup groupService = serviceGroupMapper.isRegisted(parentId, name);
		return null == groupService ? false : true;
	}

}
