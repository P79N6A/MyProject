package com.meituan.service.mobile.mtthrift.config;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/5/17
 * Time: 11:20
 */
public class ThriftClientGlobalConfig {
    private static volatile boolean enableCat = true;
    private static volatile boolean enableMtrace = true;
    private static volatile boolean heartbeatAutoDegrade = false;
    private static volatile long timeoutInterval = 1000;
    private static volatile boolean timeoutLog = false;
    private static volatile boolean enableFaultInject = false;
    private static volatile boolean enableAuth = true;

    public static boolean isEnableCat() {
        return enableCat;
    }

    public static boolean isHeartbeatAutoDegrade() {
        return heartbeatAutoDegrade;
    }

    public static long getTimeoutInterval() {
        return timeoutInterval;
    }

    public static boolean isTimeoutLog() {
        return timeoutLog;
    }

    public static boolean isEnableAuth() {
        return enableAuth;
    }

    public void setEnableAuthByMcc(boolean enableAuth) {
        ThriftClientGlobalConfig.enableAuth = enableAuth;
    }

    @Deprecated
    public void setEnableAuth(boolean enableAuth) {
        throw new RuntimeException("该方法已不支持，请通过MCC配置鉴权开关");
    }

    public void setTimeoutLog(boolean timeoutLog) {
        ThriftClientGlobalConfig.timeoutLog = timeoutLog;
    }

    public void setTimeoutInterval(long timeoutInterval) {
        ThriftClientGlobalConfig.timeoutInterval = timeoutInterval;
    }

    public void setHeartbeatAutoDegrade(boolean heartbeatAutoDegrade) {
        ThriftClientGlobalConfig.heartbeatAutoDegrade = heartbeatAutoDegrade;
    }

    public void setEnableCat(boolean enableCat) {
        ThriftClientGlobalConfig.enableCat = enableCat;
    }

    public static boolean isEnableMtrace() {
        return enableMtrace;
    }

    public void setEnableMtrace(boolean enableMtrace) {
        ThriftClientGlobalConfig.enableMtrace = enableMtrace;
    }

    public static boolean isEnableFaultInject() {
        return enableFaultInject;
    }

    public void setEnableFaultInject(boolean enableFaultInject) {
        ThriftClientGlobalConfig.enableFaultInject = enableFaultInject;
    }
}
