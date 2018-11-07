package com.meituan.service.mobile.mtthrift.falcon.model;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-2
 * Time: 上午10:35
 */
public class WorkerThreadMonitor {

    private String localip;
    private String appkey;
    private int port;
    private ThreadPoolExecutor executor;


    public WorkerThreadMonitor() {
    }

    public WorkerThreadMonitor(String localip, String appkey, int port, ThreadPoolExecutor executor) {
        this.localip = localip;
        this.appkey = appkey;
        this.port = port;
        this.executor = executor;
    }

    public String getLocalip() {
        return localip;
    }

    public void setLocalip(String localip) {
        this.localip = localip;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public String getTags() {
        return String.format("appkey=%s,port=%s", appkey, port);
    }

    public int getActiveThreadNum() {
        return executor.getActiveCount();
    }

    public int getPoolSize() {
        return executor.getPoolSize();
    }

    public int getWorkQueueSize() {
        return executor.getQueue().size();
    }

}
