package com.inspur.apigateway.data;

import javax.persistence.Transient;
import java.io.Serializable;

public class ServiceInput implements Serializable, Comparable<ServiceInput> {

    private String id;

    /*所属api主键*/
    private String apiServiceId;

    /*参数名称*/
    private String name;

    /*参数类型*/
    private String type;

    /*是否必填 1是 0 否*/
    private int required;

    /*参数描述*/
    private String description;

    /*后端参数名称*/
    private String scName;

    /*后端参数类型*/
    private String scType;

    /*后端参数是否必填*/
    private int scRequired;

    /*后端参数描述*/
    private String scDescription;

    /*后端参数排序*/
    private int scSeq;

    /*后端参数位置类型*/
    private String scParamType;

    /*入参固定值*/
    private String fixedValue;

    /**
     * 非必填参数时，是否强制携带
     * 1是 0或空 否
     * 是，默认传一个空字符串
     */
    private String fixedLoadType;

    /**
     * 加密方式
     */
    private String encryptType;

    /**
     * 解密方式
     */
    private String decryptType;

    /**
     * 解密方式
     */
    private String decryptUrl;

    /**
     * 解密方式
     */
    private String encryptUrl;


    /**
     * 有可能是 string 有可能是 MultipartFile
     */
    @Transient
    private Object value;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRequired() {
        return required;
    }

    public void setRequired(int required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScName() {
        return scName;
    }

    public void setScName(String scName) {
        this.scName = scName;
    }

    public String getScType() {
        return scType;
    }

    public void setScType(String scType) {
        this.scType = scType;
    }

    public int getScRequired() {
        return scRequired;
    }

    public void setScRequired(int scRequired) {
        this.scRequired = scRequired;
    }

    public String getScDescription() {
        return scDescription;
    }

    public void setScDescription(String scDescription) {
        this.scDescription = scDescription;
    }

    public int getScSeq() {
        return scSeq;
    }

    public void setScSeq(int scSeq) {
        this.scSeq = scSeq;
    }

    public String getScParamType() {
        return scParamType;
    }

    public void setScParamType(String scParamType) {
        this.scParamType = scParamType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int compareTo(ServiceInput o) {
        try {
            return this.scSeq - o.getScSeq();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public String getFixedLoadType() {
        return fixedLoadType;
    }

    public void setFixedLoadType(String fixedLoadType) {
        this.fixedLoadType = fixedLoadType;
    }

    public String getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(String encryptType) {
        this.encryptType = encryptType;
    }

    public String getDecryptType() {
        return decryptType;
    }

    public void setDecryptType(String decryptType) {
        this.decryptType = decryptType;
    }

    public String getDecryptUrl() {
        return decryptUrl;
    }

    public void setDecryptUrl(String decryptUrl) {
        this.decryptUrl = decryptUrl;
    }

    public String getEncryptUrl() {
        return encryptUrl;
    }

    public void setEncryptUrl(String encryptUrl) {
        this.encryptUrl = encryptUrl;
    }
}
