package com.inspur.apigateway.dao;

import com.inspur.apigateway.data.AppInstance;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AppManageMapper {

    void insert(AppInstance app);

    void delete(String appId);

    void update(AppInstance app);

    AppInstance getById(String appId);

    List<AppInstance> getAppList(Map<String, Object> param);

    AppInstance isRegisted(String appName, String userId);

    boolean isExistedAppKey(String appKey);

    List<AppInstance> getappListByUserId(Map<String, Object> param);

    List<AppInstance> getAppStatusByUserId(Map<String, Object> param);

    List<AppInstance> getAppByAppKey(String key);
}
