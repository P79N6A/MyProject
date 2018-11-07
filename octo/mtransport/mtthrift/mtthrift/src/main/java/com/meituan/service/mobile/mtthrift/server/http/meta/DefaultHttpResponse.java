package com.meituan.service.mobile.mtthrift.server.http.meta;

import com.meituan.service.mobile.mtthrift.util.Consts;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/1/2
 */
public class DefaultHttpResponse {
    private byte[] content;

    private String contentType;

    public DefaultHttpResponse() {
    }

    public DefaultHttpResponse(byte[] content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    public void generateFailContent(String errorMsg) {
        String errorInfo = "{\"isSuccess\":false, \"errorMsg\": \"" + errorMsg + "\"}";
        content = errorInfo.getBytes();
        contentType = Consts.CONTENT_TYPE_JSON;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
