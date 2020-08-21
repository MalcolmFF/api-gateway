package com.inspur.apigateway.data;

import java.io.Serializable;

/**
 * api服务监控表-新分表之后的api_service_monitor_info表对应的实体-存储入参出参信息
 */
public class ApiServiceMonitorInfo implements Serializable {

    /**
     * 主键
     */
    private String mid;

    /**
     * api_service_monitor表的id
     */
    private String monitorId;

    /**
     * 开放平台接收到的入参信息
     */
    private String openServiceInput;

    /**
     * 开放平台接收到的入参header
     */
    private String openServiceInputHeader;

    /**
     * 开放平台的返回数据
     */
    private String openServiceOutput;

    /**
     * 调用开放平台使用的方法
     */
    private String openServiceMethod;

    /**
     * 开放平台调用真实服务的入参
     */
    private String serviceInput;

    /**
     * 开放平台调用真实服务的入参header
     */
    private String serviceInputHeader;

    /**
     * 开放平台调用真实服务的返回值
     */
    private String serviceOutput;

    /**
     * 真实服务的调用方法
     */
    private String serviceMethod;


    public ApiServiceMonitorInfo() {
    }

    public String getId() {
        return mid;
    }

    public void setId(String mid) {
        this.mid = mid;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

    public String getOpenServiceInput() {
        return openServiceInput;
    }

    public void setOpenServiceInput(String openServiceInput) {
        this.openServiceInput = openServiceInput;
    }

    public String getOpenServiceInputHeader() {
        return openServiceInputHeader;
    }

    public void setOpenServiceInputHeader(String openServiceInputHeader) {
        this.openServiceInputHeader = openServiceInputHeader;
    }

    public String getOpenServiceOutput() {
        return openServiceOutput;
    }

    public void setOpenServiceOutput(String openServiceOutput) {
        this.openServiceOutput = openServiceOutput;
    }

    public String getOpenServiceMethod() {
        return openServiceMethod;
    }

    public void setOpenServiceMethod(String openServiceMethod) {
        this.openServiceMethod = openServiceMethod;
    }

    public String getServiceInput() {
        return serviceInput;
    }

    public void setServiceInput(String serviceInput) {
        this.serviceInput = serviceInput;
    }

    public String getServiceInputHeader() {
        return serviceInputHeader;
    }

    public void setServiceInputHeader(String serviceInputHeader) {
        this.serviceInputHeader = serviceInputHeader;
    }

    public String getServiceOutput() {
        return serviceOutput;
    }

    public void setServiceOutput(String serviceOutput) {
        this.serviceOutput = serviceOutput;
    }

    public String getServiceMethod() {
        return serviceMethod;
    }

    public void setServiceMethod(String serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

}