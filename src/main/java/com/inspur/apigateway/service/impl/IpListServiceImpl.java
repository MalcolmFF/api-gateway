package com.inspur.apigateway.service.impl;

import com.inspur.apigateway.dao.IpListMapper;
import com.inspur.apigateway.data.IpList;
import com.inspur.apigateway.service.IServiceIpListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("IServiceIpListService")
@RequiredArgsConstructor
public class IpListServiceImpl implements IServiceIpListService {

    final IpListMapper ipListMapper;

    public List<IpList> queryIpList(Map map) {
        return ipListMapper.query(map);
    }

    public List<IpList> getIpList(Map map) {
        return ipListMapper.getIpList(map);
    }

    public void addIpList(IpList ipList) {
        ipListMapper.insert(ipList);
    }

    public void updateIpList(IpList ipList) {
        ipListMapper.updateIpList(ipList);
    }

    public void deleteIpListById(String id) {
        ipListMapper.deleteById(id);
    }
}
