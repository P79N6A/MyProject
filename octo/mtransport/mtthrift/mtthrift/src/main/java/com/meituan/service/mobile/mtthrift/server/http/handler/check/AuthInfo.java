package com.meituan.service.mobile.mtthrift.server.http.handler.check;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/4
 */
public class AuthInfo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String className;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String msg;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> appkeyTokenMap;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<String> appkeyWhitelist;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Map<String, String>> methodAppkeyTokenMap;

    public AuthInfo(Class clazz, Map<String, String> appkeyTokenMap, Set<String> appkeyWhitelist, Map<String, Map<String, String>> methodAppkeyTokenMap) {
        this.className = clazz.getName();
        this.appkeyTokenMap = appkeyTokenMap;
        this.appkeyWhitelist = appkeyWhitelist;
        this.methodAppkeyTokenMap = methodAppkeyTokenMap;
    }

    public AuthInfo(Class clazz, String msg) {
        this.className = clazz.getName();
        this.msg = msg;
    }

    public AuthInfo(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, String> getAppkeyTokenMap() {
        return appkeyTokenMap;
    }

    public Set<String> getAppkeyWhitelist() {
        return appkeyWhitelist;
    }

    public Map<String, Map<String, String>> getMethodAppkeyTokenMap() {
        return methodAppkeyTokenMap;
    }
}
