package com.meituan.service.mobile.mtthrift.config;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/5/19
 * Time: 18:29
 */
public class ThriftServerGlobalConfig {
    private static volatile boolean enableCat = true;
    private static volatile boolean enableMtrace = true;
    private static volatile boolean enablePooledByteBuf = false;
    private static volatile boolean enableDegradation = true;
    private static volatile boolean enableAuthDebugLog = false;
    private static volatile boolean enableAuth = true;
    private static volatile boolean enableGrayAuth = false;
    private static volatile boolean enableLimit = true;
    private static volatile boolean enableAuthErrorLog = true;
    private static volatile long channelAuthTimeIntervalMillis = 60 * 1000;

    public static boolean isEnableAuth() {
        return enableAuth;
    }

    public static long getChannelAuthTimeIntervalMillis() {
        return channelAuthTimeIntervalMillis;
    }

    public void setChannelAuthTimeIntervalMillis(long channelAuthTimeIntervalMillis) {
        this.channelAuthTimeIntervalMillis = channelAuthTimeIntervalMillis;
    }

    public void setEnableCat(boolean enableCat) {
        this.enableCat = enableCat;
    }

    public static boolean isEnableMtrace() {
        return enableMtrace;
    }

    public void setEnableMtrace(boolean enableMtrace) {
        this.enableMtrace = enableMtrace;
    }

    public static boolean isEnablePooledByteBuf() {
        return enablePooledByteBuf;
    }

    public void setEnablePooledByteBuf(boolean enablePooledByteBuf) {
        this.enablePooledByteBuf = enablePooledByteBuf;
    }

    public static boolean isEnableDegradation() {
        return enableDegradation;
    }

    public void setEnableDegradation(boolean enableDegradation) {
        this.enableDegradation = enableDegradation;
    }

    public static boolean isEnableAuthDebugLog() {
        return enableAuthDebugLog;
    }

    public void setEnableAuthDebugLog(boolean enableAuthDebugLog) {
        this.enableAuthDebugLog = enableAuthDebugLog;
    }

    public void setEnableAuth(boolean enableAuth) {
        this.enableAuth = enableAuth;
    }

    public static boolean isEnableCat() {
        return enableCat;
    }

    public static boolean isEnableGrayAuth() {
        return enableGrayAuth;
    }

    public void setEnableGrayAuthByMcc(boolean enableGrayAuth) {
        ThriftServerGlobalConfig.enableGrayAuth = enableGrayAuth;
    }

    @Deprecated
    public void setEnableGrayAuth(boolean enableGrayAuth) {
        throw new RuntimeException("该方法已不支持，请通过MCC配置灰度鉴权开关");
    }

    public static boolean isEnableLimit() {
        return enableLimit;
    }

    public void setEnableLimit(boolean enableLimit) {
        ThriftServerGlobalConfig.enableLimit = enableLimit;
    }

    public static boolean isEnableAuthErrorLog() {
        return enableAuthErrorLog;
    }

    public void setEnableAuthErrorLog(boolean enableAuthErrorLog) {
        ThriftServerGlobalConfig.enableAuthErrorLog = enableAuthErrorLog;
    }
}
