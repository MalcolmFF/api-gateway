package com.inspur.apigateway.gateway;

import com.alibaba.fastjson.JSON;
import com.inspur.apigateway.data.ApiServiceMonitor;

import java.util.HashMap;
import java.util.Map;

public class Result {

    public static String constructErrorResult(String statusCode, String error_description, ApiServiceMonitor apiServiceMonitor) {
        return constructErrorResult(statusCode, error_description, error_description, apiServiceMonitor);
    }

    public static String constructErrorResult(String statusCode, String error_description, String errorNotes, ApiServiceMonitor apiServiceMonitor) {
        apiServiceMonitor.setResult(statusCode);
        apiServiceMonitor.setNotes(errorNotes);
        Map<String, Object> result = new HashMap();
        result.put("status", statusCode);
        result.put("message", "执行服务出错");
        result.put("error_description", error_description);
        return JSON.toJSONString(result);
    }

}
