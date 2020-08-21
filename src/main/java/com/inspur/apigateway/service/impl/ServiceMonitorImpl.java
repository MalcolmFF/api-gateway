package com.inspur.apigateway.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.inspur.apigateway.dao.ServiceMonitorMapper;
import com.inspur.apigateway.data.ApiServiceMonitor;

import com.inspur.apigateway.service.IServiceMonitorService;
import com.inspur.apigateway.utils.OpenServiceConstants;
import com.inspur.apigateway.utils.StringUtil;
import com.inspur.apigateway.utils.PropertiesUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static com.inspur.apigateway.gateway.HttpRequestMethod.createCloseableHttpClient;

/**
 * api服务监控表
 *
 */
@Service
@RequiredArgsConstructor
public class ServiceMonitorImpl implements IServiceMonitorService {

    private static final Log log = LogFactory.getLog(ServiceMonitorImpl.class);

    private static String apiServiceId = PropertiesUtil.getValue(OpenServiceConstants.CONF_PROPERTIES, "get_health_model_id");
    private static String getLogDataUrl = PropertiesUtil.getValue(OpenServiceConstants.CONF_PROPERTIES, "decrypt_health_model_getLogData_url");
    private static String exportExcelUrl = PropertiesUtil.getValue(OpenServiceConstants.CONF_PROPERTIES, "decrypt_health_model_exportExcel_url");

    final ServiceMonitorMapper serviceMonitorMapper;

    @Override
    public int insert(ApiServiceMonitor apiServiceMonitor) {
        int i = serviceMonitorMapper.insert(apiServiceMonitor);
        i += serviceMonitorMapper.insertInfo(apiServiceMonitor);
        return i;
    }

    @Override
    public int delete(String id) {
        return serviceMonitorMapper.deleteById(id);
    }

    @Override
    public int update(ApiServiceMonitor apiServiceMonitor) {
        return serviceMonitorMapper.update(apiServiceMonitor);
    }

    @Override
    public ApiServiceMonitor load(String id) {
        return serviceMonitorMapper.load(id);
    }

    @Override
    public List<ApiServiceMonitor> query(Map param) {
        return serviceMonitorMapper.query(param);
    }


    @Override
    public Map<String, Object> getApiMonitorList(Map<String, String> parameters) {
        Map<String, Object> mpMap = new HashMap<String, Object>();
        parameters.put("limitNum", parameters.get("limit"));
        parameters.put("startNum", parameters.get("start"));
        parameters.remove("limit");
        parameters.remove("start");
        List<ApiServiceMonitor> ApiServiceMonitors = serviceMonitorMapper.queryList(parameters);
        int total = serviceMonitorMapper.queryListCount(parameters);
        if (StringUtil.isEmpty(ApiServiceMonitors)) {
            mpMap.put("total", 0);
            mpMap.put("data", new ArrayList<ApiServiceMonitor>());
            return mpMap;
        }

        mpMap.put("total", total >= 0 ? total : ApiServiceMonitors.size());
        mpMap.put("data", ApiServiceMonitors);

        return mpMap;
    }


    @Override
    public JSONObject getMonitorInfo(String userId) {
        int dayNum = 1;
        List<String> daylist = new ArrayList<>(dayNum);
        List<Integer> dayTotalCountList = new ArrayList<>(dayNum);
        List<Integer> dayTotalSuccessCountList = new ArrayList<>(dayNum);
        List<Integer> dayAvgTimeList = new ArrayList<>(dayNum);
        List<Map<String, String>> allTotalCounts = new ArrayList<>();
        Date date = new Date();
        //获取每天日期列表
        for (int i = dayNum - 1; i >= 0; i--) {
            daylist.add(DateUtil.format(getFewDays(date, -i), "yyyy-MM-dd"));
        }
        String startDay = daylist.get(0);
        Map<String, String> dayParameter = new HashMap<>();
        if (StringUtil.isEmpty(userId)) {
            allTotalCounts = serviceMonitorMapper.getAllCount();
        } else {
            dayParameter.put("userId", userId);
            allTotalCounts = serviceMonitorMapper.getCount(dayParameter);
        }

        //获取 dayNum 天数中每天的调用量和调用成功量（result=200）
        dayParameter.put("result", "200");
        dayParameter.put("days", startDay);

        List<Map<String, String>> dayTotalCounts = serviceMonitorMapper.getDayCount(dayParameter);
        List<Map<String, String>> dayAvgTime = serviceMonitorMapper.getAvgTime(dayParameter);
        JSONObject resultJson = new JSONObject();
        if (allTotalCounts.size() == 1) {
            resultJson.put("allTotalCount", allTotalCounts.get(0).get("totalCount"));
            resultJson.put("allTotalSuccessCount", allTotalCounts.get(0).get("totalSuccessCount"));
            JSONObject dayTotalCountJson = new JSONObject();
            JSONObject dayAvgTimeJson = new JSONObject();
            for (Map<String, String> temp : dayTotalCounts) {
                dayTotalCountJson.put(temp.get("day"), temp);
            }
            for (Map<String, String> temp : dayAvgTime){
                dayAvgTimeJson.put(temp.get("day"), temp);
            }
            for (String dayText : daylist) {
                if (dayTotalCountJson.get(dayText) == null || dayTotalCountJson.getJSONObject(dayText).size() == 0) {
                    dayTotalCountList.add(0);
                    dayTotalSuccessCountList.add(0);
                } else {
                    dayTotalCountList.add(dayTotalCountJson.getJSONObject(dayText).getInteger("totalCount"));
                    dayTotalSuccessCountList.add(dayTotalCountJson.getJSONObject(dayText).getInteger("totalSuccessCount"));
                }
                if(dayAvgTimeJson.get(dayText) == null || dayAvgTimeJson.getJSONObject(dayText).size() == 0){
                    dayAvgTimeList.add(0);
                }else {
                    dayAvgTimeList.add(dayAvgTimeJson.getJSONObject(dayText).getInteger("time"));
                }
            }
            resultJson.put("dayTotalCountList", dayTotalCountList);//每天调用次数
            resultJson.put("dayTotalSuccessCountList", dayTotalSuccessCountList);//每天成功调用次数
            resultJson.put("dayAvgTimeList", dayAvgTimeList);//每天平均调用时长
        }
        resultJson.put("daylist", daylist);//日期列表
        return resultJson;
    }

    /**
     * api调用次数
     * @param type 按月统计还是按天统计 day或month
     * @param dayNum 要统计的时间，dayNum天或dayNum月
     * @return
     */
    @Override
    public JSONObject getCallVolume(String type, int dayNum, String userId, String apiId) {
        String startDay;
        List<String> daylist = new ArrayList<>(dayNum);
        List<Integer> dayTotalCountList = new ArrayList<>(dayNum);
        List<Integer> dayTotalSuccessCountList = new ArrayList<>(dayNum);
        Date date = new Date();
        if("day".equals(type)){
            //获取每天日期列表
            for (int i = dayNum - 1; i >= 0; i--) {
                daylist.add(DateUtil.format(getFewDays(date, -i), "yyyy-MM-dd"));
            }
        }else if("month".equals(type)){
            //获取每月日期列表
            for (int i = dayNum - 1; i >= 0; i--) {
                daylist.add(DateUtil.format(getFewMonthDays(date, -i), "yyyy-MM"));
            }
        } else if ("hour".equals(type)) {
            //获取每小时日期列表
            for (int i = dayNum - 1; i >= 0; i--) {
                daylist.add(DateUtil.format(getFewHoutDays(date, -i), "yyyy-MM-dd HH"));
            }
        }else {
            //获取每天日期列表
            for (int i = dayNum - 1; i >= 0; i--) {
                daylist.add(DateUtil.format(getFewDays(date, -i), "yyyy-MM-dd"));
            }
        }
        startDay = daylist.get(0);
        Map<String, String> parameter = new HashMap<>();
        parameter.put("result", "200");
        parameter.put("type", type);
        parameter.put("days", startDay);
        parameter.put("userId", userId);
        parameter.put("apiId", apiId);
        List<Map<String, String>> callVolume = serviceMonitorMapper.getCallVolume(parameter);
        JSONObject resultJson = new JSONObject();
        JSONObject dayTotalCountJson = new JSONObject();
        for (Map<String, String> temp : callVolume) {
            dayTotalCountJson.put(temp.get("day"), temp);
        }
        for (String dayText : daylist) {
            if (dayTotalCountJson.get(dayText) == null || dayTotalCountJson.getJSONObject(dayText).size() == 0) {
                dayTotalCountList.add(0);
                dayTotalSuccessCountList.add(0);
            } else {
                dayTotalCountList.add(dayTotalCountJson.getJSONObject(dayText).getInteger("totalCount"));
                dayTotalSuccessCountList.add(dayTotalCountJson.getJSONObject(dayText).getInteger("totalSuccessCount"));
            }
        }
        resultJson.put("dayTotalCountList", dayTotalCountList);
        resultJson.put("dayTotalSuccessCountList", dayTotalSuccessCountList);
        resultJson.put("daylist", daylist);
        resultJson.put("today", daylist.get(daylist.size() - 1));
        return resultJson;
    }

    @Override
    public JSONObject getTopApiInfo(int dayNum) {
//        int dayNum = 7;
        int pageSize = 5;
        String startDay = DateUtil.format(getFewDays(new Date(), -(dayNum - 1)), "yyyy-MM-dd");
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("days", startDay);
//        parameter.put("page", 0);
        parameter.put("pageSize", pageSize);
        List<Map<String, String>> topApiCount = serviceMonitorMapper.getTopApiCount(parameter);
        JSONObject resultJson = new JSONObject();
        List<String> topApiName = new ArrayList<>(topApiCount.size());
        List<String> topApiNum = new ArrayList<>(topApiCount.size());
        for (Map<String, String> temp : topApiCount) {
            topApiName.add(temp.get("api_service_name"));
            topApiNum.add(temp.get("totalCount"));
        }
        Collections.reverse(topApiName);
        Collections.reverse(topApiNum);
        resultJson.put("topApiName", topApiName);
        resultJson.put("topApiNum", topApiNum);
        return resultJson;
    }

    /**
     * 调用者信息
     * @param dayNum 要查询到天数
     * @return
     */
    @Override
    public JSONObject getTopUserInfo(int dayNum) {
        String startDay = DateUtil.format(getFewDays(new Date(), -(dayNum - 1)), "yyyy-MM-dd");
        Map<String, String> parameter = new HashMap<>();
        parameter.put("days", startDay);
        parameter.put("result", "200");
        parameter.put("pageSize", "5");
        List<Map<String, String>> topUserCount = serviceMonitorMapper.getTopUserInfo(parameter);
        List<Map<String, String>> allCount = serviceMonitorMapper.getCount(parameter);
        JSONObject resultJson = new JSONObject();
        JSONArray topUsers = JSONArray.parseArray(JSON.toJSONString(topUserCount));
        resultJson.put("topUsers", topUsers);
        if (allCount != null && allCount.size() == 1) {
            resultJson.put("totalCount", allCount.get(0).get("totalCount"));
            resultJson.put("totalSuccessCount", allCount.get(0).get("totalSuccessCount")==null?0:allCount.get(0).get("totalSuccessCount"));
        }else {
            resultJson.put("totalCount", 0);
            resultJson.put("totalSuccessCount", 0);
        }
        return resultJson;
    }

    /**
     * api调用平均时间或最大时间
     * 只算状态正常的情况（result = 200）
     * @param type max或avg
     * @param dayNum 要查询的时间
     * @return
     */
    @Override
    public JSONObject getCallTime(String type , int dayNum) {
        List<String> daylist = new ArrayList<>(dayNum);
        for (int i = dayNum - 1; i >= 0; i--) {
            daylist.add(DateUtil.format(getFewDays(new Date(), -i), "yyyy-MM-dd"));
        }
        String startDay = daylist.get(0);
        Map<String, String> parameter = new HashMap<>();
        List<Map<String, String>> times;
        parameter.put("days", startDay);
        parameter.put("result", "200");
        if("max".equals(type)){
            times = serviceMonitorMapper.getMaxTime(parameter);
        }else {
            times = serviceMonitorMapper.getAvgTime(parameter);
        }
        List<Integer> nums = new ArrayList<>(times.size());
        List<Integer> openTime = new ArrayList<>(times.size());
        JSONObject callTimes = new JSONObject();
        for (Map<String, String> temp : times) {
            callTimes.put(temp.get("day"), temp);
        }
        for (String dayText : daylist) {
            JSONObject dayTmp = callTimes.getJSONObject(dayText);
            if (callTimes.get(dayText) == null || dayTmp.size() == 0) {
                nums.add(0);
                openTime.add(0);
            } else {
                nums.add(dayTmp.getInteger("time"));
                if(dayTmp.get("openTime") == null){
                    openTime.add(0);
                }else {
                    openTime.add(dayTmp.getInteger("openTime"));
                }
            }
        }
        JSONObject resultJson = new JSONObject();
        resultJson.put("days", daylist);
        resultJson.put("nums", nums);
        resultJson.put("openTime", openTime);
        return resultJson;
    }

    @Override
    public JSONObject getTopIpInfo() {
        int dayNum = 7;
        String startDay = DateUtil.format(getFewDays(new Date(), -(dayNum - 1)), "yyyy-MM-dd");
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("days", startDay);
        List<Map<String, String>> topIpCount = serviceMonitorMapper.getTopIpCount(parameter);
        JSONObject resultJson = new JSONObject();
        resultJson.put("topIpCount", topIpCount);
        return resultJson;
    }

    @Override
    public JSONArray getActivityList(String userId) {
        JSONArray serviceDefArray = new JSONArray();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("auditStatus", OpenServiceConstants.api_audit_pass);
        parameters.put("userId", userId);
        List<Map<String, String>> serviceDefs = serviceMonitorMapper.getActivityList(parameters);
        List<Map<String, String>> serviceDefInfos = serviceMonitorMapper.getActivityListInfo(parameters);
        List<Map<String, String>> serviceAvgTimes = serviceMonitorMapper.getCallAvgTime(parameters);
        Map<String, Object> serviceDefCache = new LinkedHashMap<>();
        if (serviceDefs != null && serviceDefs.size() > 0) {
            for (Map<String, String> serviceDef : serviceDefs) {
                serviceDef.put("result", null);
                serviceDef.put("serviceTotalTime", null);
                serviceDef.put("requestTime", null);
                serviceDef.put("avgTime", null);
                serviceDefCache.put(serviceDef.get("id"), serviceDef);
            }
            if (serviceDefInfos != null && serviceDefInfos.size() > 0) {
                for (Map<String, String> serviceDefInfo : serviceDefInfos) {
                    String apiId = serviceDefInfo.get("api_service_id");
                    Map<String, String> tmp = (Map<String, String>) serviceDefCache.get(apiId);
                    if (tmp == null || tmp.size() == 0) {
                        continue;
                    }
                    if (tmp.get("requestTime") != null) {
                        continue;
                    }
                    tmp.put("result", serviceDefInfo.get("result"));
                    tmp.put("serviceTotalTime", serviceDefInfo.get("serviceTotalTime"));
                    tmp.put("requestTime", serviceDefInfo.get("requestTime"));
                    serviceDefCache.put(apiId, tmp);
                }
            }
            if (serviceAvgTimes != null && serviceAvgTimes.size() > 0) {
                for (Map<String, String> serviceAvgTime : serviceAvgTimes) {
                    String apiId = serviceAvgTime.get("api_service_id");
                    Map<String, String> tmp = (Map<String, String>) serviceDefCache.get(apiId);
                    if (tmp == null || tmp.size() == 0) {
                        continue;
                    }
                    tmp.put("avgTime", serviceAvgTime.get("avgTime"));
                    serviceDefCache.put(apiId, tmp);
                }
            }
        }
        if (serviceDefCache.size() > 0) {
            for (Map.Entry<String, Object> entry : serviceDefCache.entrySet()) {
                serviceDefArray.add(entry.getValue());
            }
        }
//        JSONObject resultJson = new JSONObject();
//        resultJson.put("data", serviceDefArray);
//        resultJson.put("total", serviceDefArray.size());
        return serviceDefArray;
    }

    @Override
    public JSONObject getActivityStatistics(String userId, int dayNum) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        String startDay = DateUtil.format(getFewDays(new Date(), -(dayNum - 1)), "yyyy-MM-dd");
        parameters.put("auditStatus", OpenServiceConstants.api_audit_pass);
        parameters.put("userId", userId);
        parameters.put("days", startDay);
        List<Map<String, Object>> serviceStatistics = serviceMonitorMapper.getActivityStatistics(parameters);
        JSONObject resultJson = new JSONObject();
        JSONArray statistic = new JSONArray();
        if (serviceStatistics != null && serviceStatistics.size() > 0) {
            Map<String, Object> tmp = serviceStatistics.get(0);
            long total = (long) tmp.get("total"), activityTotal = (long) tmp.get("activityTotal");
            JSONObject activityJson = new JSONObject();
            activityJson.put("name", "活跃个数");
            activityJson.put("value", activityTotal);
            statistic.add(activityJson);
            JSONObject noActivityJson = new JSONObject();
            noActivityJson.put("name", "未调用个数");
            noActivityJson.put("value", total - activityTotal);
            statistic.add(noActivityJson);
            resultJson.put("total", tmp.get("total"));
            resultJson.put("activityTotal", tmp.get("activityTotal"));
            resultJson.put("activityStatistics", statistic);
        } else {
            resultJson.put("total", 0);
            resultJson.put("activityTotal", 0);
            resultJson.put("activityStatistics", statistic);
        }
        return resultJson;
    }

    @Override
    public JSONObject getProportionOfCalls(String userId, int dayNum) {
        Map<String, String> parameters = new HashMap<String, String>();
        String startDay = DateUtil.format(getFewDays(new Date(), -(dayNum - 1)), "yyyy-MM-dd");
        parameters.put("auditStatus", OpenServiceConstants.api_audit_pass);
        parameters.put("userId", userId);
        parameters.put("days", startDay);
        parameters.put("pageSize", "5");
        List<Map<String, String>> serviceCalls = serviceMonitorMapper.getProportionOfCalls(parameters);
        List<Map<String, String>> allCount = serviceMonitorMapper.getCount(parameters);
        JSONObject resultJson = new JSONObject();
        JSONArray serviceCallArray = JSONArray.parseArray(JSON.toJSONString(serviceCalls));
        resultJson.put("apiInfo", serviceCallArray);
        if (allCount != null && allCount.size() == 1) {
            resultJson.put("totalCount", allCount.get(0).get("totalCount"));
            resultJson.put("totalSuccessCount", allCount.get(0).get("totalSuccessCount") == null ? 0 : allCount.get(0).get("totalSuccessCount"));
        } else {
            resultJson.put("totalCount", 0);
            resultJson.put("totalSuccessCount", 0);
        }
        return resultJson;
    }

    @Override
    public JSONObject getUserIdCallVolumeByApi(String apiId, int dayNum) {
        Map<String, String> parameters = new HashMap<String, String>();
        if(dayNum>0){
            String startDay = DateUtil.format(getFewDays(new Date(), -(dayNum - 1)), "yyyy-MM-dd");
            parameters.put("days", startDay);
        }
        parameters.put("apiId", apiId);
        List<Map<String, Object>> serviceCalls = serviceMonitorMapper.getUserIdCallVolumeByApi(parameters);
        JSONObject resultJson = new JSONObject();
        JSONArray serviceCallArray = JSONArray.parseArray(JSON.toJSONString(serviceCalls));
        resultJson.put("apiInfo", serviceCallArray);
        List<String> echartsUserName = new ArrayList<>();
        List<String> echartsUserCount = new ArrayList<>();
        for(Map<String,Object> serviceCall : serviceCalls){
            Map<String, String> data = new HashMap<>();
            String userName = StringUtil.isNotEmpty((String)serviceCall.get("user_name"))?serviceCall.get("user_name").toString():serviceCall.get("caller_user_id").toString();
            echartsUserName.add(userName);
            String userCount = StringUtil.isNotEmpty(serviceCall.get("count").toString())?serviceCall.get("count").toString():"0";
            echartsUserCount.add(userCount);
        }
        resultJson.put("echartsUserName",echartsUserName);
        resultJson.put("echartsUserCount",echartsUserCount);
        return resultJson;
    }

    @Override
    public void exportExcelById(String monitorId, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> queryParam = new HashMap<String, String>();
        queryParam.put("id", monitorId);
        queryParam.put("apiServiceId", "b983cc8ee7814d2d9d4702fa08f230e7");
        queryParam.put("result", "200");
        List<ApiServiceMonitor> apiServiceMonitors = serviceMonitorMapper.query(queryParam);
        ApiServiceMonitor apiServiceMonitor = null;
        if (apiServiceMonitors != null && apiServiceMonitors.size() == 1) {
            apiServiceMonitor = apiServiceMonitors.get(0);
            String openServiceOutput = apiServiceMonitor.getOpenServiceOutput();
            JSONObject openServiceOutputJson = JSON.parseObject(openServiceOutput);
            String data = openServiceOutputJson.getString("data");
            JSONObject instream = new JSONObject();
            instream.put("content", data);
            exportExcel(exportExcelUrl, instream.toString(), request, response);
        } else {
            log.warn("导出Excel失败，记录不存在。monitorId = [" + monitorId + "]");
        }

    }

    @Override
    public List<Map<String, Object>> queryNotSuccessNearby(Map<String, Object> paramsMap) {
        return serviceMonitorMapper.queryNotSuccessNearby(paramsMap);
    }

    public Date getFewDays(Date date, int dayNum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, dayNum);
        return calendar.getTime();
    }

    public Date getFewMonthDays(Date date, int dayNum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, dayNum);
        return calendar.getTime();
    }

    public Date getFewHoutDays(Date date, int dayHourNum) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, dayHourNum);
        return calendar.getTime();
    }


    public void exportExcel(String url, String instream, HttpServletRequest request, HttpServletResponse response) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", OpenServiceConstants.content_type_json);
        httpPost.setEntity(new StringEntity(instream, "utf-8"));
        try (final CloseableHttpClient httpclient = createCloseableHttpClient(url);
             final CloseableHttpResponse httpResponse = httpclient.execute(httpPost)) {
            Header contentType = httpResponse.getFirstHeader("Content-Type");
            Header dispositionHeader = httpResponse.getFirstHeader("Content-disposition");
            Header transferEncodingHeader = httpResponse.getFirstHeader("Transfer-Encoding");
            HttpEntity entity = httpResponse.getEntity();
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            InputStream inputStream = null;
            if (entity != null) {
                inputStream = entity.getContent();
                response.reset();
                response.setContentType(contentType.getValue());
                response.setHeader(dispositionHeader.getName(), dispositionHeader.getValue());
                response.setHeader(transferEncodingHeader.getName(), transferEncodingHeader.getValue());
            } else {
                inputStream = new ByteArrayInputStream("Export Excel failed ".getBytes());
                response.reset();
                response.setContentType(OpenServiceConstants.content_type_html);
            }
            try {
                ServletOutputStream sout = response.getOutputStream();
                bis = new BufferedInputStream(inputStream);
                bos = new BufferedOutputStream(sout);
                byte[] buff = new byte[2048];
                int bytesRead;
                // Simple read/write loop.
                while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                    bos.write(buff, 0, bytesRead);
                }
            } catch (Exception e) {
                log.error("导出excel出现异常:", e);
            } finally {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            }
            EntityUtils.consume(entity);
        } catch (Exception e) {
            log.error("调用导出Excel接口错误，URL:" + url, e);
        }
    }

    @Override
    public Integer queryCountGroupByApi(String apiServiceId) {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("apiServiceId", apiServiceId);
        return serviceMonitorMapper.queryCountGroupByApi(parameter);
    }
}
