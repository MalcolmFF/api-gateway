package com.inspur.apigateway.controller;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.RateLimiter;
import com.inspur.apigateway.data.*;
import com.inspur.apigateway.gateway.*;
import com.inspur.apigateway.pay.data.PayAccountCapital;
import com.inspur.apigateway.pay.service.IPayService;
import com.inspur.apigateway.service.*;
import com.inspur.apigateway.utils.OpenServiceConstants;
import com.inspur.apigateway.utils.IPUtil;
import com.inspur.apigateway.utils.StringUtil;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.loushang.framework.util.DateUtil;
import org.loushang.framework.util.UUIDGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.inspur.apigateway.utils.OpenServiceConstants.*;
import static com.inspur.apigateway.utils.ThreadUtil.getNcpu;

@Controller
@RequiredArgsConstructor
public class ServiceExecuteController {

    private static Log log = LogFactory.getLog(ServiceExecuteController.class);

    final IAppManage appManage;
    final IPayService payService;
    final IDevGroupService devGroupService;
    final IServiceDefService serviceDefService;
    final IServiceMonitorService monitorService;
    final IServiceApplyService serviceApplyService;
    final IServiceInputService serviceInputService;
    final IServiceIpListService serviceIpListService;

    /**
     * API服务转发主入口
     * @param apiContext
     * @param reqPath
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping("/api/execute/do/{apiContext}/{reqPath}")
    @ResponseBody
    public void execute(@PathVariable("apiContext") String apiContext,
                        @PathVariable("reqPath") String reqPath,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {

        PrintWriter writer ;
        long startTime = 0;
        long openStartTime = System.currentTimeMillis();
        String requestTime = DateUtil.getCurrentTime2();
        String responseTime;
        String requestUserId = "";
        String instream = "";
        String result_str = "";
        BigDecimal servicePrice = new BigDecimal(0);
        ServiceDef serviceDef = null;
        String logId = UUIDGenerator.getUUID();

        // 构造日志模型
        ApiServiceMonitor apiServiceMonitor = new ApiServiceMonitor();
        apiServiceMonitor.setId(logId);
        apiServiceMonitor.setMonitorId(logId);
        apiServiceMonitor.setRequestTime(requestTime);
        apiServiceMonitor.setOpenServiceRequestURL(request.getRequestURL().toString());
        log.info(logId + " **************** 接受请求 ****************");

        try {
            // 获取请求者IP
            String requestIp = IPUtil.getClientIp(request);
            apiServiceMonitor.setCallerIp(requestIp);
            apiServiceMonitor.setOpenServiceMethod(request.getMethod());
            apiServiceMonitor.setOpenServiceInput(JSON.toJSONString(request.getParameterMap()));

            //  ---------------- 2 IP黑名单 start----------------
            log.info(logId+" ---- 1.开始查询数据库黑名单和白名单");
            Map<String,Object> ipParams = new HashMap<>();
            ipParams.put("ipV4",requestIp);
            ipParams.put("active","true");
            ipParams.put("type","white");
            List<IpList> ips_white = serviceIpListService.getIpList(ipParams);  // 查白名单
            ipParams.put("type","black");
            List<IpList> ips_black = serviceIpListService.getIpList(ipParams); // 查黑名单

            // 存在于黑名单但不存在于白名单，拒绝访问
            if (ips_black.size()>0 && ips_white.size() ==0){
                result_str = Result.constructErrorResult(ASM_ERROR_IP_REFUSE,"IP地址被禁用",apiServiceMonitor);
                return;
            }
            //  ---------------- 2 IP黑名单 end ----------------
            log.info(logId + " ---- 2.IP黑名单校验结束");


            // ---------------- 3 查询APP start ----------------
            response.setCharacterEncoding("utf-8");
            response.addHeader("Content-Type", OpenServiceConstants.content_type_html);
            //组装头部并获取 appkey、appSecret 和请求签名signature
            Enumeration<String> headNames = request.getHeaderNames();
            Map<String, String> headers = new HashMap<>();
            String appkey ;
            String appSecret ;
            String signature ;

            // 1.先获取参数中的密钥（page转发时）
            appkey = request.getParameter("xCaKey");
            signature = request.getParameter("xCaSignature");

            // 2.api服务时获取请求头中的密钥
            while (headNames.hasMoreElements()) {
                String headName = headNames.nextElement();
                headers.put(headName, request.getHeader(headName));
                if (headName.toUpperCase().equals(SystemHeader.X_CA_KEY.toUpperCase())) {
                    appkey = request.getHeader(headName);
                }
                if (headName.toUpperCase().equals(SystemHeader.X_CA_SIGNATURE.toUpperCase())) {
                    signature = request.getHeader(headName);
                }
            }
            if (!StringUtils.isNotBlank(appkey)){
                result_str = Result.constructErrorResult(ASM_ERROR_APP_EMPTY, "请传入appkey",apiServiceMonitor);
                return;
            }
            apiServiceMonitor.setOpenServiceInputHeader(JSONObject.fromObject(headers).toString());
            List<AppInstance> appList = appManage.getAppByAppKey(appkey);
            if (appList == null || appList.size() != 1) {
                result_str = Result.constructErrorResult(ASM_ERROR_APP_UNAUTHORIZE, "应用未授权或不存在",apiServiceMonitor);
                return;
            }
            appSecret = appList.get(0).getAppSecret();
            String appId = appList.get(0).getAppId();
            requestUserId = appList.get(0).getUserId();
            apiServiceMonitor.setCallerAppId(appId);
            apiServiceMonitor.setCallerUserId(requestUserId);
            // ---------------- 3 查询APP end ----------------
            log.info(logId + " ---- 3.查询应用信息结束");


            // ---------------- 4 判断签名正确性 start ----------------
            if (!appSecret.equals(signature)) {
                result_str = Result.constructErrorResult(ASM_ERROR_SIGNATURE, "验证签名不正确",apiServiceMonitor);
                return;
            }
            log.info(logId + " ---- 4.验证签名结束");
            // ---------------- 4 判断签名正确性 end ----------------


            // ---------------- 5 通过context,reqPath关联查询API是否存在和状态 start ----------------
            serviceDef = checkApiService(apiContext, reqPath, apiServiceMonitor);
            if (serviceDef == null) {
                result_str = Result.constructErrorResult(ASM_ERROR_SERVICE, "API服务不存在",apiServiceMonitor);
                return;
            }
            String apiServiceId = serviceDef.getId();
            apiServiceMonitor.setApiServiceId(apiServiceId);
            apiServiceMonitor.setApiServiceName(serviceDef.getName());
            if (!OpenServiceConstants.api_audit_pass.equals(serviceDef.getAuditStatus())) {
                result_str = Result.constructErrorResult(ASM_ERROR_SERVICE_NO_PASS, "API服务当前状态不可用",apiServiceMonitor);
                return;
            }
            // ---------------- 5 通过context,reqPath关联查询API是否存在和状态 end ----------------
            log.info(logId + " ---- 5.API是否存在和状态结束[" + apiServiceId + "][" + serviceDef.getName() + "]");


            // ---------------- 6 判断是否允许公网访问 start ----------------
            String url =request.getRequestURL().toString().replace(request.getRequestURI(),"").split("//")[1].split(":")[0];
            String pattern = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
            if(!serviceDef.getPublicNet() && !url.matches(pattern)){
                result_str = Result.constructErrorResult(ASM_ERROR_PUBLICNET, "API服务不允许公网访问",apiServiceMonitor);
                return;
            }
            // ---------------- 6 判断是否允许公网访问 end ----------------
            log.info(logId + " ---- 6.判断是否允许公网访问结束");


            // ---------------- 7 通过serviceId和appId查询API授权状态 start ----------------
            Map<String,String> applymap = new HashMap<>();
            applymap.put("apiServiceId", apiServiceId);
            applymap.put("appId", appId);
            applymap.put("authStatus", OpenServiceConstants.auth_status_pass);
            List<ServiceApply> alist = serviceApplyService.getList(applymap);
            if (alist == null || alist.size() == 0) {
                result_str = Result.constructErrorResult(ASM_ERROR_SERVICE_UNAUTHORIZE_APP, "API未授权应用",apiServiceMonitor);
                return;
            }
            // ---------------- 7 通过serviceId和appId查询授权记录 end ----------------
            log.info(logId + " ---- 7.通过serviceId和appId查询授权记录结束");


            // ---------------- 8 余额判断 start ----------------
            servicePrice = serviceDef.getPrice();
            if (servicePrice.compareTo(new BigDecimal("0.00")) > 0) {
                if (StringUtils.isNotEmpty(requestUserId)) {
                    // 查询账户余额
                    PayAccountCapital payAccountCapital = payService.getPayAccountByUserId(requestUserId);
                    BigDecimal balance = new BigDecimal(payAccountCapital.getAccountBalance());
                    if (balance.subtract(serviceDef.getPrice()).compareTo(new BigDecimal("0.00")) <= 0) {
                        result_str = Result.constructErrorResult(ASM_ERROR_BALANCE, "账户余额不足，请及时充值",apiServiceMonitor);
                        return;
                    }
                }
            }
            // ---------------- 8 余额判断 end ----------------
            log.info(logId + " ---- 8.判断余额结束");


            // ---------------- 9 入参初始化 start ----------------
            List<ServiceInput> listServiceInput = serviceInputService.listByServiceId(apiServiceId);
            try {
                //读取request数据流
                String contentType = request.getHeader("Content-Type");
                if (StringUtils.isNotEmpty(contentType) && !contentType.equals(OpenServiceConstants.SC_TYPE_APPLICATION_XWWWFORMURLENCODED)) {
                    instream = HttpRequestMethod.getRequestIn(request);
                    if (StringUtil.isNotEmpty(instream)) {
                        apiServiceMonitor.setOpenServiceInput(instream);
                    }
                }
                // 给入参赋值
                initInputList(request, requestUserId, listServiceInput);
            } catch (Exception e) {
                result_str = Result.constructErrorResult(ASM_ERROR_PARAMETER, "输入参数异常:"+e.getMessage(), apiServiceMonitor);
                return;
            }
            // ---------------- 9 入参初始化 end ----------------
            log.info(logId + " ---- 9.入参初始化结束");


            //  ---------------- 10 限流判断 start ----------------
            String key = apiContext+"/"+reqPath;
            // 如果是首次请求
            if (GatewayConstant.map.get(key) == null){
                GatewayConstant.map.putIfAbsent(key, RateLimiter.create( serviceDef.getLimitCount() != null ? serviceDef.getLimitCount() : GatewayConstant.default_limitCount ));
            }
            // 进行限流
            RateLimiter rateLimiter = (RateLimiter) GatewayConstant.map.get(key);
            if (!rateLimiter.tryAcquire()) {
                result_str = Result.constructErrorResult(ASM_ERROR_QPS_LIMIT, "请求过于频繁", apiServiceMonitor);
                return;
            }
            //  ---------------- 10 限流判断 end ----------------
            log.info(logId + " ---- 10.限流判断结束");


            // ---------------- 11 执行转发请求 start ----------------
            startTime = System.currentTimeMillis();
            if (serviceDef.getApiType()!=null && serviceDef.getApiType().equals("page")){
                // 1.服务类型为页面，执行重定向；
                log.info(logId + " ---- 11.doRequest开始（page）");
                String urlTarget = httpUrlBuffer(serviceDef, listServiceInput, apiServiceMonitor);
                apiServiceMonitor.setResult("200");
                apiServiceMonitor.setResult(ASM_SUCCESS);
                response.sendRedirect(urlTarget);
            }else{
                log.info(logId + " ---- 11.执行转发开始");
                if (serviceDef.getScProtocol().equals("webService")) {
                    String type = serviceDef.getScFrame();
                    if ("Axiom".equals(type)) {
                        result_str = Dispatcher.executeAxis2(serviceDef, listServiceInput, apiServiceMonitor);
                    } else if ("RPCAxis".equals(type)) {
                        result_str = Dispatcher.executeRPCAxis(serviceDef, listServiceInput, apiServiceMonitor);
                    } else if ("RPCAxis2".equals(type)) {
                        result_str = Dispatcher.executeRPCAxisClient(serviceDef, listServiceInput, apiServiceMonitor);
                    }
                } else {
                    result_str = Dispatcher.execHttpRequest(response, instream, serviceDef, listServiceInput, apiServiceMonitor);
                }
            }
            // ---------------- 11 执行转发请求 end ----------------
            log.info(logId + " ---- 12.执行转发结束。");
        } catch (Throwable e) {
            result_str = Result.constructErrorResult(ASM_ERROR_UNKNOWN, "内部错误", "内部错误:"+e.toString(), apiServiceMonitor);
            log.info(logId + " ---- 12.目标服务调用出错" + e.toString());
        } finally {
            // 第三方接口耗时
            long serviceTime = startTime == 0 ? 0 : System.currentTimeMillis() - startTime;

            // 调用成功时，执行扣款
            if (StringUtils.equals("200",apiServiceMonitor.getResult())) {
                apiServiceMonitor.setResult(ASM_SUCCESS);
                apiServiceMonitor.setNotes("成功");
                if (requestUserId != null && servicePrice != null) {
                    try{
                        if (servicePrice.compareTo(new BigDecimal("0.00")) > 0) {
                            payService.subPayAccountByUserId(requestUserId, servicePrice + "");
                        }
                    }catch (Exception e){
                        log.info(logId + "执行扣款失败："+e.toString());
                    }
                }
                log.info(logId + " ---- 13.执行扣款结束");
            }

            // 进行响应，并关闭writer流
            if (serviceDef == null || !serviceDef.getContentType().equals("binary")){
                writer = response.getWriter();
                writer.print(result_str);
                writer.flush();
                IOUtils.closeQuietly(writer);
                log.info(logId + " ---- 14.执行响应并关闭writer流结束");
            }

            // 总耗时和响应时间
            long openServiceTime = System.currentTimeMillis() - openStartTime;
            responseTime = DateUtil.getCurrentTime2();

            // 持久化日志模型
            apiServiceMonitor.setOpenServiceOutput(result_str);  // 平台响应内容
            apiServiceMonitor.setOpenServiceTotalTime((int) openServiceTime); // 平台总耗时
            apiServiceMonitor.setServiceTotalTime((int) serviceTime); // 调用第三方接口耗时
            apiServiceMonitor.setResponseTime(responseTime); // 平台响应时间
            apiServiceMonitor.setCreateTime(responseTime);  // 日志写入时间
            IPUtil.insertByThreadPool(monitorService, apiServiceMonitor, GatewayConstant.monitorExecutorService);

            log.info(logId + " ---- 15.finally api开放平台处理耗时[" + (openServiceTime - serviceTime) + "]，第三方耗时[" + serviceTime + "]，总耗时[" + openServiceTime + "]毫秒");
            log.info(logId + " ---- 16.打印日志记录："+JSON.toJSONString(apiServiceMonitor));
        }
    }
    

    /**
     * 检查api服务是否存在可用
     */
    public ServiceDef checkApiService(String apiContext, String reqPath, ApiServiceMonitor apiServiceMonitor) {
        //根据apiContext和reqPath查询唯一的API service
        ServiceDef serviceDef = null;
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("context", apiContext);
        tmp.put("reqPath", "/" + reqPath);
        String context_path = "/" + apiContext + "/" + reqPath;
        List<ServiceDef> defs2 = serviceDefService.getByGroupContextAndPath(tmp);
        if (defs2 == null || defs2.size() != 1) {
            apiServiceMonitor.setNotes("API服务不存在,context_path=[" + context_path + "]");
            apiServiceMonitor.setResult(ASM_ERROR_SERVICE);
        } else {
            serviceDef = defs2.get(0);
        }
        return serviceDef;
    }

    /**
     * 校验参数类型
     * @param type 参数类型
     * @param value 参数值
     * @param key 参数名
     * @param required 是否必填
     * @param postionType 参数位置
     * @throws Exception 异常
     */
    private void checkData(String type, String value, String key,Integer required,String postionType) throws Exception {
        // 如果是必填参数，校验是否为空
        if (required == 1){
            // body参数 raw类型，不执行校验
            if (StringUtils.equals(postionType,"body") && (type.toLowerCase().equals("application/json") || type.toLowerCase().equals("application/xml") )){
                return;
            }
            if (value == null){
                throw new Exception("请输入必填参数[" + key + "]");
            }
        }
        if (type.toLowerCase().equals("application/json")) {
            try {
                JSON.parseObject(value);
            } catch (Exception e) {
                throw new Exception("applicaiton/json数据格式错误[" + key + "=" + value + "]");
            }
        }
        if (type.toLowerCase().equals("int")) {
            try {
                Integer.parseInt(value);
            } catch (Exception e) {
                throw new Exception("int数据格式错误[" + key + "=" + value + "]");
            }
        }
        if (type.toLowerCase().equals("long")) {
            try {
                Long.parseLong(value);
            } catch (Exception e) {
                throw new Exception("long数据格式错误[" + key + "=" + value + "]");
            }
        }
        if (type.toLowerCase().equals("double")) {
            try {
                Double.parseDouble(value);
            } catch (Exception e) {
                throw new Exception("double数据格式错误[" + key + "=" + value + "]");
            }
        }
        if (type.toLowerCase().equals("float")) {
            try {
                Float.parseFloat(value);
            } catch (Exception e) {
                throw new Exception("float数据格式错误[" + key + "=" + value + "]");
            }
        }
        if (type.toLowerCase().equals("boolean")) {
            try {
                System.out.println(Boolean.parseBoolean(value));
            } catch (Exception e) {
                throw new Exception("boolean数据格式错误[" + key + "=" + value + "]");
            }
        }
    }

    /**
     * 验证输入参数并赋值
     * @param request http请求
     * @param userId 调用者Id
     * @param listServiceInput 入参列表
     * @throws Exception 异常
     */
    public void initInputList(HttpServletRequest request, String userId, List<ServiceInput> listServiceInput) throws Exception {
        for (ServiceInput serviceInput : listServiceInput) {
            String paramName = serviceInput.getName();
            String paramType = serviceInput.getType();
            String postionType = serviceInput.getScParamType();
            String fixedValue = serviceInput.getFixedValue();
            int required = serviceInput.getRequired();

            // 判断是文件类型还是基本类型
            if (StringUtils.equals(paramType, OpenServiceConstants.SC_TYPE_FILE)){
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

                MultipartFile files = multipartRequest.getFile(paramName);
                serviceInput.setValue(files);
            }else{
                // 有设置固定值
                if (StringUtils.isNotEmpty(fixedValue)) {
                    // 先进行赋值。 （如果依然传了，进行覆盖）
                    if("#userId#".toUpperCase().equals(fixedValue.toUpperCase())){
                        //如果需要从开放平台获取调用者的userId的参数，需要在必填项里面填上#userId#
                        serviceInput.setValue(userId);
                    }else {
                        serviceInput.setValue(fixedValue);
                    }
                }

                // 先从请求头中获取（适配可能出现的老方式）
                if (StringUtils.equals(postionType, OpenServiceConstants.SC_PARAMTYPE_HEAD)) {
                    String value = request.getHeader(paramName);
                    if (value != null) { // 防止null将固定值覆盖
                        serviceInput.setValue(value);
                    }
                }

                // 从参数中获取
                String value = request.getParameter(paramName);
                if (value != null){ // 防止null将固定值覆盖
                    serviceInput.setValue(value);
                }

                // 校验 (参数类型和必填性)
                checkData(paramType, (String)serviceInput.getValue(), paramName,required,postionType);
            }
        }
    }

    /**
     * 页面转发拼装URL
     * @param serviceDef api定义
     * @param listServiceInput 入参
     * @param apiServiceMonitor 日志实体类
     * @return 拼接的URL
     * @throws Exception 异常
     */
    private String httpUrlBuffer(ServiceDef serviceDef, List<ServiceInput> listServiceInput, ApiServiceMonitor apiServiceMonitor) throws Exception {

        String addr = serviceDef.getScAddr();
        addr = addr.endsWith("/") ? addr.substring(0, addr.length() - 1) : addr;
        StringBuilder httpurl = new StringBuilder();
        httpurl.append(addr);

        //组装后端参数param
        Map<String, String> header = new HashMap<>();
        JSONObject serviceInputParam = new JSONObject();
        header.put("Accept", "*/*");
        Collections.sort(listServiceInput);//根据scSeq排序

        boolean firstQueryParam = true;
        for (ServiceInput serviceInput : listServiceInput) {
            serviceInputParam.put(serviceInput.getScName(), serviceInput.getValue());
            //判断必填属性
            if (1 == serviceInput.getRequired() && serviceInput.getValue()!=null && !serviceInput.getScType().equals("text/xml") && !serviceInput.getScType().equals("application/json")) {
                throw new Exception("请传入必填参数" + serviceInput.getName());
            }

            // 执行加解密，接口级别和参数级别
            Object decryptedParam = EncoderDecoder.decryptedParam(serviceInput, serviceDef.getEncryptionType());
            serviceInputParam.put(serviceInput.getScName(), decryptedParam);

            checkData(serviceInput.getScType(), (String)decryptedParam, serviceInput.getScName(),serviceInput.getRequired(),serviceInput.getScParamType());//判断参数类型是否正确
            String paramType = serviceInput.getScParamType();

            if (paramType.equalsIgnoreCase(OpenServiceConstants.SC_PARAMTYPE_QUERY)) {
//                if (StringUtils.equals(serviceInput.getScName(),"xCaKey") || StringUtils.equals(serviceInput.getScName(),"xCaSignature") ){
//                    continue;
//                }
                if (firstQueryParam) {
                    if (decryptedParam != null) {
                        httpurl.append("?").append(serviceInput.getScName()).append("=").append(URLEncoder.encode((String)decryptedParam,"UTF-8"));
                        firstQueryParam = false;
                    }
                } else {
                    if (decryptedParam != null) {
                        httpurl.append("&").append(serviceInput.getScName()).append("=").append(URLEncoder.encode((String)decryptedParam,"UTF-8"));
                    }
                }
            }
        }

        apiServiceMonitor.setServiceInput(serviceInputParam.toString());
        apiServiceMonitor.setServiceInputHeader(JSONObject.fromObject(header).toString());
        apiServiceMonitor.setServiceMethod(serviceDef.getScHttpMethod().toUpperCase());
        return httpurl.toString();
    }
}
