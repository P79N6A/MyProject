package com.meituan.service.mobile.mtthrift.server.http.handler.check;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/4
 */
public class SignInfo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String className;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String msg;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> localTokenMap;

    public SignInfo(Class clazz, Map<String, String> localTokenMap) {
        this.className = clazz.getName();
        this.localTokenMap = localTokenMap;
    }

    public SignInfo(Class clazz, String msg) {
        this.className = clazz.getName();
        this.msg = msg;
    }

    public SignInfo(String msg) {
        this.msg = msg;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, String> getLocalTokenMap() {
        return localTokenMap;
    }

    public String getMsg() {
        return msg;
    }
}
