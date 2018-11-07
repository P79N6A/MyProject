package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.auth.AuthMetaData;
import com.meituan.service.mobile.mtthrift.auth.AuthType;
import com.meituan.service.mobile.mtthrift.auth.IAuthHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-12-28
 * Time: 下午2:54
 */
public class UserDefinedAuthHandler implements IAuthHandler {

    private Set<String> appkeyWhitelist = new HashSet<String>();

    public UserDefinedAuthHandler() {
    }

    public UserDefinedAuthHandler(Set<String> appkeyWhitelist) {
        this.appkeyWhitelist = appkeyWhitelist;
    }

    public boolean auth(AuthMetaData authMetaData) {
        String appkey = authMetaData.getClientAppkey();
        if (appkeyWhitelist != null && appkey != null && appkeyWhitelist.contains(appkey)) {
            return true;
        }
        return false;
    }

    public AuthType getAuthType() {
        return AuthType.requestAuth;
    }

    public Set<String> getAppkeyWhitelist() {
        return appkeyWhitelist;
    }

    public void setAppkeyWhitelist(Set<String> appkeyWhitelist) {
        this.appkeyWhitelist = appkeyWhitelist;
    }

}
