package com.meituan.service.mobile.mtthrift.server.http.handler.check;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class ServerInfo {

    private String appkey;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PortServiceInfo> serviceInfo;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ServiceMethodInfo> serviceMethods;

    // 基本信息
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String env;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String swimlane;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String startTime;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String version;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public List<PortServiceInfo> getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(List<PortServiceInfo> serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public List<ServiceMethodInfo> getServiceMethods() {
        return serviceMethods;
    }

    public void setServiceMethods(List<ServiceMethodInfo> serviceMethods) {
        this.serviceMethods = serviceMethods;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }


}
