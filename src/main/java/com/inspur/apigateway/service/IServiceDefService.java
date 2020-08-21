package com.inspur.apigateway.service;

import com.inspur.apigateway.data.ServiceDef;

import java.util.List;
import java.util.Map;

public interface IServiceDefService {
    List<ServiceDef> listServiceDefs(Map map);

    ServiceDef getServiceDef(String id);

    void update(ServiceDef serviceDef);

    void addServiceDef(ServiceDef serviceDef);

    void updateServiceDef(ServiceDef serviceDef);

    void deleteById(String id);

    List<ServiceDef> getByApiGroupAndPath(Map map);

    List<ServiceDef> getByGroupContextAndPath(Map map);

    List<ServiceDef> listAPIByProvider(Map map);

    List<ServiceDef> queryByRemoteId(String remoteId);

    int updateServiceDefCount(Map<String, String> map);
}
