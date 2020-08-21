package com.inspur.apigateway.data;

import javax.persistence.Transient;
import java.io.Serializable;

public class ServiceApply implements Serializable{

    private String id;

    private String app_id;

    private String app_name;

    private String api_service_id;

    private String api_service_name;

    private String api_provider;

    private String applicant;

    private  String apply_time;

    private  String auth_status;

    private  String auth_time;

    private String auth_user;

    private String apply_flag;

    private String batch_apply_id;

    @Transient
    private String version;

    @Transient
    private  String name;

    @Transient
    private String apiStatus;

    @Transient
    private  String provider;

    @Transient
    private  String audit_status;



    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAudit_status() {
        return audit_status;
    }

    public void setAudit_status(String audit_status) {
        this.audit_status = audit_status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getApi_service_id() {
        return api_service_id;
    }

    public void setApi_service_id(String api_service_id) {
        this.api_service_id = api_service_id;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getApply_time() {
        return apply_time;
    }

    public void setApply_time(String apply_time) {
        this.apply_time = apply_time;
    }

    public String getAuth_status() {
        return auth_status;
    }

    public void setAuth_status(String auth_status) {
        this.auth_status = auth_status;
    }

    public String getAuth_time() {
        return auth_time;
    }

    public void setAuth_time(String auth_time) {
        this.auth_time = auth_time;
    }

    public String getAuth_user() {
        return auth_user;
    }

    public void setAuth_user(String auth_user) {
        this.auth_user = auth_user;
    }

    public String getApply_flag() {
        return apply_flag;
    }

    public void setApply_flag(String apply_flag) {
        this.apply_flag = apply_flag;
    }

    public String getApi_service_name() {
        return api_service_name;
    }

    public void setApi_service_name(String api_service_name) {
        this.api_service_name = api_service_name;
    }

    public String getApi_provider() {
        return api_provider;
    }

    public void setApi_provider(String api_provider) {
        this.api_provider = api_provider;
    }

    public void setApiStatus(String apiStatus) {
        this.apiStatus = apiStatus;
    }
    public String getApiStatus(){
        return this.apiStatus;
    }

    public String getBatch_apply_id() {
        return batch_apply_id;
    }

    public void setBatch_apply_id(String batch_apply_id) {
        this.batch_apply_id = batch_apply_id;
    }
}
