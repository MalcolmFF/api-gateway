package com.inspur.apigateway.gateway;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.inspur.apigateway.utils.ThreadUtil.getNcpu;

public class GatewayConstant {
    // 默认限流器大小
    public static Double default_limitCount = 60.0;

    // 每次路由的地址，创建的限流器
    public static ConcurrentHashMap map = new ConcurrentHashMap();

    public static ExecutorService monitorExecutorService = Executors.newFixedThreadPool(getNcpu() * 2);
}
