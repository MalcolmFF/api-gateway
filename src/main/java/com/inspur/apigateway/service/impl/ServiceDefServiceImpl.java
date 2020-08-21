package com.inspur.apigateway.service.impl;

import com.inspur.apigateway.dao.ServiceDefMapper;
import com.inspur.apigateway.dao.ServiceInputMapper;
import com.inspur.apigateway.dao.ServiceOutputMapper;
import com.inspur.apigateway.data.ServiceDef;
import com.inspur.apigateway.data.ServiceInput;
import com.inspur.apigateway.data.ServiceOutput;
import com.inspur.apigateway.service.IServiceDefService;
import com.inspur.apigateway.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.loushang.framework.util.DateUtil;
import org.loushang.framework.util.UUIDGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("serviceDefService")
@RequiredArgsConstructor
public class ServiceDefServiceImpl implements IServiceDefService{

    final private ServiceDefMapper serviceDefMapper;
    final private ServiceInputMapper serviceInputMapper;
    final private ServiceOutputMapper serviceOutputMapper;

    @Override
    public List<ServiceDef> listServiceDefs(Map data){
       return serviceDefMapper.listServiceDefs(data);
    }

    @Override
    public ServiceDef getServiceDef(String id){
        return serviceDefMapper.getById(id);
    }

    @Override
    public void update(ServiceDef serviceDef){
        serviceDefMapper.audit(serviceDef);
    }

    @Override
    public void addServiceDef(ServiceDef serviceDef) {
        List<ServiceInput> list=serviceDef.getInputList();
        if (list == null){
            list = new ArrayList<>();
        }
        serviceDefMapper.insert(serviceDef);
        for(ServiceInput input:list){
            input.setId(UUIDGenerator.getUUID());
            input.setApiServiceId(serviceDef.getId());
            serviceInputMapper.insert(input);
        }
        List<ServiceOutput> outputs=serviceDef.getOutputList();
        if (outputs == null){
            outputs = new ArrayList<>();
        }
        for (ServiceOutput output:outputs){
            output.setId(UUIDGenerator.getUUID());
            output.setOpenServiceId(serviceDef.getId());
            serviceOutputMapper.insert(output);
        }
    }

    @Override
    public void updateServiceDef(ServiceDef serviceDef) {
        List<ServiceInput> list=serviceDef.getInputList();
        serviceDefMapper.update(serviceDef);
        //删除以前所有
        serviceInputMapper.deleteByApiId(serviceDef.getId());
        serviceOutputMapper.deleteByApiId(serviceDef.getId());
        //重新添加
        for(ServiceInput input:list){
            input.setId(UUIDGenerator.getUUID());
            input.setApiServiceId(serviceDef.getId());
            serviceInputMapper.insert(input);
        }
        List<ServiceOutput> outputs=serviceDef.getOutputList();
        for (ServiceOutput output:outputs){
            output.setId(UUIDGenerator.getUUID());
            output.setOpenServiceId(serviceDef.getId());
            serviceOutputMapper.insert(output);
        }
    }

    @Override
    public void deleteById(String id) {
        //删除def
        serviceDefMapper.deleteById(id);
        //删除对应的输入参数
        serviceInputMapper.deleteByApiId(id);
        //删除对应的输出参数
        serviceOutputMapper.deleteByApiId(id);
    }

    @Override
    public List<ServiceDef> getByApiGroupAndPath(Map map) {
        return serviceDefMapper.getByApiGroupAndPath(map);
    }

    @Override
    public List<ServiceDef> getByGroupContextAndPath(Map map) {
        return serviceDefMapper.getByGroupContextAndPath(map);
    }

    @Override
    public List<ServiceDef> listAPIByProvider(Map map){
        return serviceDefMapper.listAPIByProvider(map);
    }
    @Override
    public List<ServiceDef> queryByRemoteId(String remoteId){
        if (StringUtils.isEmpty(remoteId)){
            return null;
        }
       return serviceDefMapper.queryByRemoteId(remoteId);
    }


    @Override
    public int updateServiceDefCount(Map<String, String> map) {
        if (StringUtil.isEmpty(map.getOrDefault("updateTime", ""))) {
            map.put("updateTime", DateUtil.getCurrentTime2());
        }
        return serviceDefMapper.updateServiceDefCount(map);
    }

}
