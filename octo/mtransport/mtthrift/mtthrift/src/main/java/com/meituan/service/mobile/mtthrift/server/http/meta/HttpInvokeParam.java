package com.meituan.service.mobile.mtthrift.server.http.meta;

import java.util.List;

public class HttpInvokeParam {

    private String clientAppkey;
    private String serverAppkey;
    private String serviceName;
    private String methodName;
    private List<MethodParameter> parameters;
    private boolean isTest = true;

    public HttpInvokeParam() {}

    public HttpInvokeParam(String clientAppkey, String serverAppkey, String serviceName) {
        this.clientAppkey = clientAppkey;
        this.serverAppkey = serverAppkey;
        this.serviceName = serviceName;
    }

    public String getClientAppkey() {
        return clientAppkey;
    }

    public void setClientAppkey(String clientAppkey) {
        this.clientAppkey = clientAppkey;
    }

    public String getServerAppkey() {
        return serverAppkey;
    }

    public void setServerAppkey(String serverAppkey) {
        this.serverAppkey = serverAppkey;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<MethodParameter> parameters) {
        this.parameters = parameters;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }
}
