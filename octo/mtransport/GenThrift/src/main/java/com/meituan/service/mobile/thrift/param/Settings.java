package com.meituan.service.mobile.thrift.param;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-1-30
 * Time: 上午11:32
 */
public class Settings {

    public String commonDir;
    public String thriftDir092;
    public String thriftDir080;
    public String serverIP;


    public String clientSecret;
    public String oauthUrl;

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getOauthUrl() {
        return oauthUrl;
    }

    public void setOauthUrl(String oauthUrl) {
        this.oauthUrl = oauthUrl;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public String getCommonDir() {
        return commonDir;
    }

    public void setCommonDir(String commonDir) {
        this.commonDir = commonDir;
    }

    public String getThriftDir092() {
        return thriftDir092;
    }

    public void setThriftDir092(String thriftDir092) {
        this.thriftDir092 = thriftDir092;
    }

    public String getThriftDir080() {
        return thriftDir080;
    }

    public void setThriftDir080(String thriftDir080) {
        this.thriftDir080 = thriftDir080;
    }
}
