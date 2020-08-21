package com.inspur.apigateway.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.inspur.apigateway.data.ApiServiceMonitor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IServiceMonitorService {

    int insert(ApiServiceMonitor apiServiceMonitor);

    int delete(String id);

    int update(ApiServiceMonitor apiServiceMonitor);

    ApiServiceMonitor load(String id);

    List<ApiServiceMonitor> query(Map param);

    Map<String, Object> getApiMonitorList(Map<String, String> parameters);

    JSONObject getMonitorInfo(String userId);

    JSONObject getCallVolume(String type, int dayNum, String userId, String apiId);

    JSONObject getTopApiInfo(int dayNum);

    JSONObject getTopUserInfo(int dayNum);

    JSONObject getCallTime(String type, int dayNum);

    JSONObject getTopIpInfo();

    JSONArray getActivityList(String userId);

    JSONObject getActivityStatistics(String userId, int dayNum);

    JSONObject getProportionOfCalls(String userId, int dayNum);

    JSONObject getUserIdCallVolumeByApi(String apiId, int dayNum);

    void exportExcelById(String monitorId, HttpServletRequest request, HttpServletResponse response);

    List<Map<String, Object>> queryNotSuccessNearby(Map<String, Object> paramsMap);

    Integer queryCountGroupByApi(String id);
}
