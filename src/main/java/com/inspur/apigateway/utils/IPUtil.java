package com.inspur.apigateway.utils;

import com.inspur.apigateway.data.ApiServiceMonitor;
import com.inspur.apigateway.service.IServiceMonitorService;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

public class IPUtil {

    public IPUtil() {
    }

    public static void insert(IServiceMonitorService monitorService, ApiServiceMonitor apiServiceMonitor) {
        ThreadUtil asmThread = new ThreadUtil(monitorService, apiServiceMonitor);
        asmThread.run();
    }

    public static void insertByThreadPool(IServiceMonitorService monitorService, ApiServiceMonitor apiServiceMonitor, ExecutorService executorService) {
        executorService.submit(new ThreadUtil(monitorService, apiServiceMonitor));
    }

    /**
     * 根据request请求获取IP地址
     *
     * @param request
     */
    public static String getClientIp(HttpServletRequest request) {
        String ipAddress = null;
        // ipAddress = this.getRequest().getRemoteAddr();
        ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-Ip");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ipAddress = inet.getHostAddress();
            }
        }
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }
        return ipAddress;
    }
}
