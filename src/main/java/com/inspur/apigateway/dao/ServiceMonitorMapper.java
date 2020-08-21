package com.inspur.apigateway.dao;

import com.inspur.apigateway.data.ApiServiceMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.loushang.framework.mybatis.mapper.EntityMapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ServiceMonitorMapper extends EntityMapper<ApiServiceMonitor> {

    @Override
    int insert(ApiServiceMonitor apiServiceMonitor);

    int insertInfo(ApiServiceMonitor apiServiceMonitor);

    int deleteById(String id);

    @Override
    int update(ApiServiceMonitor apiServiceMonitor);

    ApiServiceMonitor load(String id);

    @Override
    List<ApiServiceMonitor> query(Map param);

    List<ApiServiceMonitor> queryList(Map param);

    int queryListCount(Map param);

    List<Map<String, String>> getAllCount();

    List<Map<String, String>> getCount(Map<String, String> map);

    List<Map<String, String>> getDayCount(Map<String, String> map);

    List<Map<String, String>> getAvgTime(Map<String, String> map);

    List<Map<String, String>> getMaxTime(Map<String, String> map);

    List<Map<String, String>> getCallVolume(Map<String, String> map);

    List<Map<String, String>> getTopApiCount(Map<String, Object> map);

    List<Map<String, String>> getTopUserInfo(Map<String, String> map);

    List<Map<String, String>> getTopIpCount(Map<String, Object> map);

    List<Map<String, Object>> getActivityStatistics(Map<String, Object> map);

    List<Map<String, String>> getProportionOfCalls(Map<String, String> map);

    List<Map<String, Object>> getUserIdCallVolumeByApi(Map<String, String> map);

    List<Map<String, String>> getCallAvgTime(Map<String, String> map);

    List<Map<String, String>> getActivityList(Map<String, String> map);

    List<Map<String, String>> getActivityListInfo(Map<String, String> map);

    List<Map<String, Object>> queryNotSuccessNearby(Map<String, Object> paramsMap);

    Integer queryCountGroupByApi(Map<String, Object> parameter);
}
