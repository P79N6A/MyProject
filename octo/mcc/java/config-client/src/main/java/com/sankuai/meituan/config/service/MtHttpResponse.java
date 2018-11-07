package com.sankuai.meituan.config.service;

import java.util.Map;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-20
 */
public class MtHttpResponse {
    private int statusCode = -1;
    private Map<String, String> headers;
    private String entity;

    public void MtHttpResponse() {

    }
    public String getHeader(String key) {
        if (key == null) {
            return null;
        }
        return headers.get(key);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }
}
