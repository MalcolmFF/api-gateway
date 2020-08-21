package com.inspur.apigateway.data;

import java.io.Serializable;

/**
 * api服务监控表-新分表之后的api_service_monitor表对应实体
 */
public class ApiServiceMonitorBase implements Serializable {

    /**
     * 主键
     */
    private String id;

    /**
     * api_service_def表的id，api服务的主键
     */
    private String apiServiceId;

    /**
     * api服务名称
     */
    private String apiServiceName;

    /**
     * 调用开放平台的URL
     */
    private String openServiceRequestURL;

    /**
     * api服务调用结果编码
     */
    private String result;

    /**
     * 调用者ip地址
     */
    private String callerIp;

    /**
     * 调用者的应用id
     */
    private String callerAppId;

    /**
     * 调用者id
     */
    private String callerUserId;

    /**
     * 请求时间
     */
    private String requestTime;

    /**
     * 响应时间
     */
    private String responseTime;

    /**
     * 真实api接口调用时长，单位毫秒
     */
    private Integer serviceTotalTime;

    /**
     * 开放平台接口调用总时长，单位毫秒
     */
    private Integer openServiceTotalTime;

    /**
     * 备注
     */
    private String notes;

    /**
     * 创建时间
     */
    private String createTime;


    public ApiServiceMonitorBase() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApiServiceId() {
        return apiServiceId;
    }

    public void setApiServiceId(String apiServiceId) {
        this.apiServiceId = apiServiceId;
    }

    public String getApiServiceName() {
        return apiServiceName;
    }

    public void setApiServiceName(String apiServiceName) {
        this.apiServiceName = apiServiceName;
    }

    public String getOpenServiceRequestURL() {
        return openServiceRequestURL;
    }

    public void setOpenServiceRequestURL(String openServiceRequestURL) {
        this.openServiceRequestURL = openServiceRequestURL;
    }

    public Integer getOpenServiceTotalTime() {
        return openServiceTotalTime;
    }

    public void setOpenServiceTotalTime(Integer openServiceTotalTime) {
        this.openServiceTotalTime = openServiceTotalTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCallerIp() {
        return callerIp;
    }

    public void setCallerIp(String callerIp) {
        this.callerIp = callerIp;
    }

    public String getCallerAppId() {
        return callerAppId;
    }

    public void setCallerAppId(String callerAppId) {
        this.callerAppId = callerAppId;
    }

    public String getCallerUserId() {
        return callerUserId;
    }

    public void setCallerUserId(String callerUserId) {
        this.callerUserId = callerUserId;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    public Integer getServiceTotalTime() {
        return serviceTotalTime;
    }

    public void setServiceTotalTime(Integer serviceTotalTime) {
        this.serviceTotalTime = serviceTotalTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}