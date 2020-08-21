package com.inspur.apigateway.data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class ServiceDef {

    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 审核状态
     */
    private String auditStatus;
    /**
     * 审核人
     */
    private String auditUser;
    /**
     * 授权类型
     */
    private String authType;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 上线时间
     */
    private String onlineTime;
    /**
     * 分组id
     */
    private String groupId;
    /**
     * 提供者
     */
    private String provider;
    /**
     * 更新时间
     */
    private String updateTime;
    /**
     * 数据远程唯一标识
     */
    private String remoteId;
    /**
     * 通讯协议
     */
    private String protocol;
    /**
     * 请求path
     */
    private String reqPath;
    /**
     * 请求方法 get、post
     */
    private String httpMethod;
    /**
     * 后端请求协议
     */
    private String scProtocol;
    /**
     * 后端请求方法 get、post
     */
    private String scHttpMethod;
    /**
     * 后端服务全路径
     */
    private String scAddr;
    /**
     * 返回contentType
     */
    private String contentType;
    /**
     * 返回示例
     */
    private String returnSample;
    /**
     * api 分组 对应DevGroup 类
     */
    private String apiGroup;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 收费类型 默认1
     */
    private String priceType;
    /**
     * 加密类型 默认0=不加密
     */
    private String encryptionType;
    /**
     * 最大每秒查询率,默认1.0，单位次/秒
     */
    private String maxQps;
    /**
     * 最大限流大小，单位次/秒
     */
    private Double limitCount;
    /**
     * 每个调用者的接口调用上限
     */
    private Integer topLimitCount;
    /**
     * 每个调用者接口调用上限时间粒度
     */
    private String topLimitUnit;
    /**
     * api类型
     */
    private String apiType;

    private String scFrame;

    private String nameSpace;

    private String sc_ws_function;

    private String callCount;
    /**
     * returntypeqname，qname格式，axis参数
     */
    private String wsReturnType;
    /**
     * returnqname，qname，axis参数
     */
    private String wsReturnQname;
    /**
     * 是否允许公网访问，‘0’为不允许，‘1’为允许
     */
    private Boolean publicNet;

    /**
     * form表单的形式
     * multipart/form-data: form-data
     * application/x-www-form-urlencoded: x-www-form-urlencoded
     */
    private String formType;

    @Transient
    private List<ServiceInput> inputList;

    @Transient
    private List<ServiceOutput> outputList;

    @Transient
    private String openAddr;




    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public String getCallCount() {
        return callCount;
    }

    public void setCallCount(String callCount) {
        this.callCount = callCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(String auditUser) {
        this.auditUser = auditUser;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(String onlineTime) {
        this.onlineTime = onlineTime;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getReqPath() {
        return reqPath;
    }

    public void setReqPath(String reqPath) {
        this.reqPath = reqPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<ServiceInput> getInputList() {
        return inputList;
    }

    public String getScProtocol() {
        return scProtocol;
    }

    public void setScProtocol(String scProtocol) {
        this.scProtocol = scProtocol;
    }

    public String getScHttpMethod() {
        return scHttpMethod;
    }

    public void setScHttpMethod(String scHttpMethod) {
        this.scHttpMethod = scHttpMethod;
    }

    public String getScAddr() {
        return scAddr;
    }

    public void setScAddr(String scAddr) {
        this.scAddr = scAddr;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getReturnSample() {
        return returnSample;
    }

    public void setReturnSample(String returnSample) {
        this.returnSample = returnSample;
    }

    public String getApiGroup() {
        return apiGroup;
    }

    public void setApiGroup(String apiGroup) {
        this.apiGroup = apiGroup;
    }

    public void setInputList(List<ServiceInput> inputList) {
        this.inputList = inputList;
    }

    public String getOpenAddr() {
        return openAddr;
    }

    public void setOpenAddr(String openAddr) {
        this.openAddr = openAddr;
    }

    public void setOutputList(List<ServiceOutput> outputList) {
        this.outputList = outputList;
    }

    public List<ServiceOutput> getOutputList() {
        return outputList;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

//    public String getMaxQps() {
//        return maxQps;
//    }
//
//    public void setMaxQps(String maxQps) {
//        this.maxQps = maxQps;
//    }

    public Double getLimitCount() {
        return limitCount;
    }

    public void setLimitCount(Double limitCount) {
        this.limitCount = limitCount;
    }

    public Integer getTopLimitCount() {
        return topLimitCount;
    }

    public void setTopLimitCount(Integer topLimitCount) {
        this.topLimitCount = topLimitCount;
    }

    public String getTopLimitUnit() {
        return topLimitUnit;
    }

    public void setTopLimitUnit(String topLimitUnit) {
        this.topLimitUnit = topLimitUnit;
    }

    public String getScFrame() {
        return scFrame;
    }

    public void setScFrame(String scFrame) {
        this.scFrame = scFrame;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getSc_ws_function() {
        return sc_ws_function;
    }

    public void setSc_ws_function(String sc_ws_function) {
        this.sc_ws_function = sc_ws_function;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    public String getWsReturnType() {
        return wsReturnType;
    }

    public void setWsReturnType(String wsReturnType) {
        this.wsReturnType = wsReturnType;
    }

    public String getWsReturnQname() {
        return wsReturnQname;
    }

    public void setWsReturnQname(String wsReturnQname) {
        this.wsReturnQname = wsReturnQname;
    }

    public Boolean getPublicNet() {
        return publicNet;
    }

    public void setPublicNet(Boolean publicNet) {
        this.publicNet = publicNet;
    }
}
