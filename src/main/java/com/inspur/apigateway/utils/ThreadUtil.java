package com.inspur.apigateway.utils;

import com.inspur.apigateway.data.ApiServiceMonitor;
import com.inspur.apigateway.service.IServiceMonitorService;


public class ThreadUtil extends Thread {
    private IServiceMonitorService monitorService;
    private ApiServiceMonitor apiServiceMonitor;
    private static final int Ncpu = Runtime.getRuntime().availableProcessors();

    public ThreadUtil(IServiceMonitorService monitorService, ApiServiceMonitor apiServiceMonitor) {
        this.apiServiceMonitor = apiServiceMonitor;
        this.monitorService = monitorService;
    }

    @Override
    public void run() {
        this.monitorService.insert(this.apiServiceMonitor);
    }

    public static int getNcpu() {
        return Ncpu;
    }
}
