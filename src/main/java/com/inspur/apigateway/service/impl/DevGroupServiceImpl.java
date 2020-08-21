package com.inspur.apigateway.service.impl;

import com.inspur.apigateway.dao.DevGroupMapper;
import com.inspur.apigateway.data.DevGroup;
import com.inspur.apigateway.service.IDevGroupService;
import lombok.RequiredArgsConstructor;
import org.loushang.framework.util.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("devGroupService")
@RequiredArgsConstructor
public class DevGroupServiceImpl implements IDevGroupService{

    final DevGroupMapper devGroupMapper;

    @Override
    public int insert(DevGroup group) {
        String groupId = UUIDGenerator.getUUID();
        group.setId(groupId);
        group.setContext(groupId);
        return devGroupMapper.insert(group);
    }

    @Override
    public void deletebyId(String id) {

         devGroupMapper.deletebyId(id);
    }

    @Override
    public void update(DevGroup group) {

          devGroupMapper.update(group);
    }

    @Override
    public DevGroup getById(String id) {
        return devGroupMapper.getById(id);
    }

    @Override
    public List<DevGroup> getGroupList(Map<String, Object> param) {
        return devGroupMapper.getGroupList(param);
    }

    @Override
    public boolean isRegisted(String userId,String name) {
        DevGroup devGroup=devGroupMapper.isRegisted(userId,name);
        return null == devGroup ? false : true;
    }
    @Override
    public boolean isExistedContext(String context) {
        DevGroup devGroup=devGroupMapper.isExistedContext(context);
        return null == devGroup ? false : true;
    }
}
