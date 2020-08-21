package com.inspur.apigateway.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OpenServiceConstants {

    // 项目配置文件
    public static String CONF_PROPERTIES = "conf.properties";
    /**
     * 输入参数 scParamType 后端入参位置
     */
    public final static String SC_PARAMTYPE_BODY = "body";
    public final static String SC_PARAMTYPE_PATH = "path";
    public final static String SC_PARAMTYPE_HEAD = "head";
    public final static String SC_PARAMTYPE_QUERY = "query";
    /**
     *输入参数类型
     */
    public final static String SC_TYPE_APPLICATION_JSON = "application/json";
    public final static String SC_TYPE_TEXT_XML = "text/xml";
    public final static String SC_TYPE_APPLICATION_XWWWFORMURLENCODED = "application/x-www-form-urlencoded";
    public final static String SC_TYPE_FILE = "file";
    /*
    ApiServiceMonitor表，api监控审计用到的错误代码等常量
     */
    public final static String ASM_SUCCESS = "200";

    //API分组错误
    public final static String ASM_ERROR_GROUP = "10001";

    //API服务不存在
    public final static String ASM_ERROR_SERVICE = "10002";

    //API服务当前状态不可用
    public final static String ASM_ERROR_SERVICE_NO_PASS = "10003";

    //查询授权应用异常
    public final static String ASM_ERROR_APP_UNAUTHORIZE = "10004";

    //API未授权应用
    public final static String ASM_ERROR_SERVICE_UNAUTHORIZE_APP = "10005";

    //验证签名不正确
    public final static String ASM_ERROR_SIGNATURE = "10006";

    //账户余额不足
    public final static String ASM_ERROR_BALANCE = "10007";

    //输入参数异常
    public final static String ASM_ERROR_PARAMETER = "10008";

    // ip在黑名单里
    public final static String ASM_ERROR_IP = "10009";

    // 超过限流值
    public final static String ASM_ERROR_LIMIT = "10010";

    //API服务不允许公网访问
    public final static String ASM_ERROR_PUBLICNET = "10011";

    //未知错误
    public final static String ASM_ERROR_UNKNOWN = "99999";

    public final static String ASM_ERROR_IP_REFUSE = "20201";
    public final static String ASM_ERROR_QPS_LIMIT = "20302";

    // 未获取到appKey
    public final static String ASM_ERROR_APP_EMPTY = "10012";

    // 第三方接口返回状态码异常（不为200）
    public final static String ASM_ERROR_STATUS_CODE = "50101";

    // 调用第三方接口失败
    public final static String ASM_ERROR_REQUEST = "60101";
    public final static String ASM_ERROR_REQUEST_TEXT = "开放平台暂时无法访问此接口，请稍后再试";


    public static String api_create = "0";//api创建
    public static String api_submit_audit = "1";//api提交审核(发布)
    public static String api_audit_pass = "2";//审核api通过
    public static String api_audit_reject = "3";//审核api驳回
    public static String api_offline = "4";//api下线


    public static String auth_status_submit = "0";//代授权
    public static String auth_status_pass = "1";//授权通过
    public static String auth_status_reject = "2";//授权驳回

    public static String system_user = "@system";//通过系统推送数据的默认用户
    public static String masert_realm = "master";//管理员域名

    public static String auth_type_no = "0";//不需要授权
    public static String auth_type_yes = "1";//需要授权


    public static String content_type_json = "application/json;charset=utf-8";
    public static String content_type_text = "text/plain;charset=utf-8";
    public static String content_type_binary = "application/octet-stream;charset=utf-8";
    public static String content_type_xml = "application/xml;charset=utf-8";
    public static String content_type_text_xml = "text/xml;charset=utf-8";
    public static String content_type_html = "text/html;charset=utf-8";

    /**
     * 每个调用者接口调用上限时间粒度
     */
    public final static String TOP_LIMIT_UNIT_MINUTE = "minute";
    public final static String TOP_LIMIT_UNIT_HOUR = "hour";
    public final static String TOP_LIMIT_UNIT_DAY = "day";
    public final static String TOP_LIMIT_UNIT_WEEK = "week";
    public final static String TOP_LIMIT_UNIT_MONTH = "month";

    public static final Map<String, String> TOP_LIMIT_UNIT_MAP;
    /**
     * 加密方式
     */
    public final static String ENCRYPT_MODE_NO = "0";
    public final static String ENCRYPT_MODE_KEY_BASE64 = "BASE64";
    public final static String ENCRYPT_MODE_KEY_MD5 = "MD5";
    public final static String ENCRYPT_MODE_KEY_SHA_1 = "SHA-1";
    public final static String ENCRYPT_MODE_KEY_SHA_256 = "SHA-256";
    public final static String ENCRYPT_MODE_KEY_SHA_384 = "SHA-384";
    public final static String ENCRYPT_MODE_KEY_SHA_512 = "SHA-512";
    public final static String ENCRYPT_MODE_KEY_SM3 = "SM3";
    public final static String ENCRYPT_MODE_KEY_REST = "REST";


    public static final Map<String, String> ENCRYPTION_MAP;

    static {
        Map aMap = new HashMap();
        aMap.put(ENCRYPT_MODE_NO, "不加密");
        aMap.put(ENCRYPT_MODE_KEY_BASE64, ENCRYPT_MODE_KEY_BASE64);
        aMap.put(ENCRYPT_MODE_KEY_MD5, ENCRYPT_MODE_KEY_MD5);
        aMap.put(ENCRYPT_MODE_KEY_SHA_1, ENCRYPT_MODE_KEY_SHA_1);
        aMap.put(ENCRYPT_MODE_KEY_SM3, ENCRYPT_MODE_KEY_SM3);
        ENCRYPTION_MAP = Collections.unmodifiableMap(aMap);

        Map bMap = new LinkedHashMap();
        bMap.put(TOP_LIMIT_UNIT_MINUTE, "每分钟");
        bMap.put(TOP_LIMIT_UNIT_HOUR, "每小时");
        bMap.put(TOP_LIMIT_UNIT_DAY, "每天");
        bMap.put(TOP_LIMIT_UNIT_WEEK, "每星期");
        bMap.put(TOP_LIMIT_UNIT_MONTH, "每月");
        TOP_LIMIT_UNIT_MAP = Collections.unmodifiableMap(bMap);
    }

}
