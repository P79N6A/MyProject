package com.meituan.service.mobile.mtthrift.monitor;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 2017/4/5
 * Time: 上午11:50
 */
public class NullClientMonitor implements IClientMonitor {

    @Override
    public void noticeInvoke(String serviceName, String methodName, String serverIpPort, long takesMills) {

    }

    @Override
    public void noticeGetConnect(String serviceName, long takesMills) {

    }

    @Override
    public void noticeException(String serviceName, String methodName, String serverIpPort, String exceptionMessage, Throwable e) {

    }

}
