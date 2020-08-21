package com.inspur.apigateway.service.impl;

import com.inspur.apigateway.dao.ServiceApplyMapper;
import com.inspur.apigateway.data.ServiceApply;
import com.inspur.apigateway.service.IServiceApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("serviceApplyService")
@RequiredArgsConstructor
public class ServiceApplyImpl implements IServiceApplyService{

    final ServiceApplyMapper serviceApplyMapper;

    @Override
   public List<ServiceApply> getServiceAuthList(Map<String, Object> parameters){
        return  serviceApplyMapper.getServiceAuthList(parameters);
    }
    @Override
    public List<ServiceApply> getServiceList(Map<String, Object> param){
        return  serviceApplyMapper.getServiceList(param);
    }
    @Override
    public List<ServiceApply> getApplyList(Map<String, Object> param){
        return  serviceApplyMapper.getApplyList(param);
    }

    @Override
    public String insert(ServiceApply serviceApply) {
        try{
            serviceApplyMapper.insert(serviceApply);
            return  "true";
        }catch (Exception e)
        {
            e.printStackTrace();
            return "false";
        }

    }
    @Override
    public ServiceApply getById(String id)
    {
        return serviceApplyMapper.get(id);
    }

    @Override
    public List<Map<String,Object>> getByBatchId(String id)
    {
        return serviceApplyMapper.getByBatchId(id);
    }

    @Override
    public void updateServiceApply(ServiceApply serviceApply){
        serviceApplyMapper.update(serviceApply);
    }

    @Override
    public List<ServiceApply> getByServiceId(String apiServiceId) {
        return serviceApplyMapper.getByServiceId(apiServiceId);
    }

    @Override
    public  List<ServiceApply> getListById(Map<String, Object> param) {
        return serviceApplyMapper.getListById(param);
    }


    public  List<ServiceApply> getAuthorizedApiListById(Map<String, Object> param){
        return serviceApplyMapper.getAuthorizedApiListById(param);
    }

    @Override
    public List<ServiceApply> getList(Map<String, String> param){
        return serviceApplyMapper.getList(param);
    }

    @Override
    public List<ServiceApply> isApplyAuthToUser(Map<String, Object> map) {
        return serviceApplyMapper.isApplyAuthToUser(map);
    }

    public int updateById(ServiceApply serviceApply){
        return serviceApplyMapper.updateById(serviceApply);
    }

    @Override
    public List<ServiceApply> getAPIAuthList(Map<String, Object> parameters){
        return  serviceApplyMapper.getAPIAuthList(parameters);
    }
    @Override
    public void deleteApplyById(String id){
        serviceApplyMapper.deleteApplyById(id);
    }

}
