package com.meituan.control.zookeeper.http;

import java.util.HashMap;


/**
 * User: jinmengzhe
 * Date: 2015-05-25
 */
public class MtHttpRequest {
    private String method = "";
    private String endpoint = "";
    private HashMap<String, String> headers = new HashMap<String, String>();
    private HashMap<String, String> parameters = new HashMap<String, String>();
    private byte[] contentData = null;

    public MtHttpRequest(String endpoint, String method) {
        this.endpoint = endpoint;
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public byte[] getContentData() {
        return contentData;
    }

    public void setContentData(byte[] contentData) {
        this.contentData = contentData;
    }
}
