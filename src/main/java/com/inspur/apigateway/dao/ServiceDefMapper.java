package com.inspur.apigateway.dao;

import com.inspur.apigateway.data.ServiceDef;
import org.apache.ibatis.annotations.Mapper;
import org.loushang.framework.mybatis.mapper.EntityMapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ServiceDefMapper extends EntityMapper<ServiceDef> {

    List<ServiceDef> getServiceDef(Map map);

    List<ServiceDef> listServiceDefs(Map map);

    ServiceDef getById(String id);

    void audit(ServiceDef serviceDef);

    void deleteById(String id);

    List<ServiceDef> getByApiGroupAndPath(Map map);

    List<ServiceDef> getByGroupContextAndPath(Map map);

    List<ServiceDef> listAPIByProvider(Map map);

    List<ServiceDef> queryByRemoteId(String remoteId);

    int updateServiceDefCount(Map<String, String> map);
}
