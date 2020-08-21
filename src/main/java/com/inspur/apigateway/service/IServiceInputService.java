package com.inspur.apigateway.service;

import com.inspur.apigateway.data.ServiceInput;

import java.util.List;

public interface IServiceInputService {

	List<ServiceInput> listByServiceId(String serviceId);
}
