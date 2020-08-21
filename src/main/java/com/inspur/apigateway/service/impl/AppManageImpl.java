package com.inspur.apigateway.service.impl;

import com.inspur.apigateway.dao.AppManageMapper;
import com.inspur.apigateway.data.AppInstance;
import com.inspur.apigateway.service.IAppManage;
import lombok.RequiredArgsConstructor;
import org.loushang.framework.util.UUIDGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("appManage")
@RequiredArgsConstructor
public class AppManageImpl implements IAppManage {

    final AppManageMapper appManageMapper;

    @Override
    public String insert(AppInstance app) {
        String appId = UUIDGenerator.getUUID();
        app.setAppId(appId);
        appManageMapper.insert(app);
        return appId;
    }

    @Override
    public void delete(String appId) {
        appManageMapper.delete(appId);
    }

    @Override
    public void update(AppInstance app) {
        appManageMapper.update(app);
    }

    @Override
    public AppInstance getById(String appId) {
        return appManageMapper.getById(appId);
    }

    @Override
    public List<AppInstance> getAppList(Map<String, Object> param) {
        return appManageMapper.getAppList(param);
    }

    @Override
    public boolean isRegisted( String appName,String userId) {
        AppInstance app = appManageMapper.isRegisted(appName,userId);
        return null == app ? false : true;
    }
    @Override
    public boolean isExistedAppKey(String appKey){
        return appManageMapper.isExistedAppKey(appKey);

    }

    @Override
    public List<AppInstance> getappListByUserId(Map<String,Object> param)
    {
        return  appManageMapper.getappListByUserId(param);
    }

    @Override
    public List<AppInstance> getAppStatusByUserId(Map<String,Object> param)
    {
        return  appManageMapper.getAppStatusByUserId(param);
    }

    @Override
    public int returnAppkey(){
        int appKey=(int)((Math.random()*9+1)*100000);
        boolean isExistedAppKey = appManageMapper.isExistedAppKey(String.valueOf(appKey));;//生成的随机数进入数据库中查询一下，看是否有相同的。
        if(isExistedAppKey){
            return returnAppkey();
        }else{//否则
            return appKey;
        }
    }
    @Override
    public List<AppInstance> getAppByAppKey(String key) {
        return appManageMapper.getAppByAppKey(key);
    }
}
