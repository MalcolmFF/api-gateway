package com.inspur.apigateway.dao;

import com.inspur.apigateway.data.IpList;
import org.apache.ibatis.annotations.Mapper;
import org.loushang.framework.mybatis.mapper.EntityMapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface IpListMapper extends EntityMapper<IpList> {

    void deleteById(String id);

    List<IpList> getIpList(Map map);

    int updateIpList(IpList ipList);
}
