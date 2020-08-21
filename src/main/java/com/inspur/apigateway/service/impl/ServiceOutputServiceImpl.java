package com.inspur.apigateway.service.impl;

import com.inspur.apigateway.dao.ServiceOutputMapper;
import com.inspur.apigateway.data.ServiceOutput;
import com.inspur.apigateway.service.IServiceOutputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceOutputServiceImpl implements IServiceOutputService {

    final ServiceOutputMapper serviceOutputMapper;

    @Override
    public List<ServiceOutput> selectByApiId(String apiId){
        return serviceOutputMapper.selectByApiId(apiId);
    }
}
