package com.inspur.apigateway.service;

import com.inspur.apigateway.data.ServiceOutput;

import java.util.List;

public interface IServiceOutputService {
    List<ServiceOutput> selectByApiId(String apiI);
}
