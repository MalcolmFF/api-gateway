package com.inspur.apigateway.service;

import com.inspur.apigateway.data.ServiceApply;

import java.util.List;
import java.util.Map;

public interface IServiceApplyService {
    List<ServiceApply> getServiceAuthList(Map<String, Object> parameters);

    List<ServiceApply> getServiceList(Map<String, Object> param);

    List<ServiceApply> getApplyList(Map<String, Object> param);

    String insert(ServiceApply serviceApply);

    ServiceApply getById(String id);

    List<Map<String,Object>> getByBatchId(String id);

    void updateServiceApply(ServiceApply serviceApply);

    List<ServiceApply> getByServiceId(String apiServiceId);

    List<ServiceApply> getAuthorizedApiListById(Map<String, Object> param);

    List<ServiceApply> getListById(Map<String, Object> param);

    List<ServiceApply> getList(Map<String, String> param);

    int updateById(ServiceApply serviceApply);

    List<ServiceApply> isApplyAuthToUser(Map<String, Object> map);

    List<ServiceApply> getAPIAuthList(Map<String, Object> parameters);

    void deleteApplyById(String id);
}