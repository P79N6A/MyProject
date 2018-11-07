package com.meituan.service.mobile.thrift.model;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-5-20
 * Time: 下午8:48
 */
public class OauthResult {

    private OauthInfo data;
    private Boolean isSuccess;

    public OauthInfo getData() {
        return data;
    }

    public void setData(OauthInfo data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }
}
