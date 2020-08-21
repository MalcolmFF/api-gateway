package com.inspur.apigateway.service.impl;

import com.inspur.apigateway.dao.ServiceInputMapper;
import com.inspur.apigateway.data.ServiceInput;
import com.inspur.apigateway.service.IServiceInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("serviceInputService")
@RequiredArgsConstructor
public class ServiceInputServiceImpl implements IServiceInputService {

	final private ServiceInputMapper serviceInputMapper;

	@Override
	public List<ServiceInput> listByServiceId(String serviceId) {
		Map<String,Object> map=new HashMap<>();
		map.put("apiServiceId",serviceId);
		return serviceInputMapper.listServiceInputs(map);
	}
}
