package com.meituan.service.mobile.mtthrift.server.http.handler.check;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class PortServiceInfo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String port;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ServiceIfaceInfo> serviceIfaceInfos;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AuthInfo authInfo;

    public PortServiceInfo(String port, List<ServiceIfaceInfo> serviceIfaceInfos, String status) {
        this.port = port;
        this.serviceIfaceInfos = serviceIfaceInfos;
        this.status = status;
    }

    public PortServiceInfo(String port, AuthInfo authInfo) {
        this.port = port;
        this.authInfo = authInfo;
    }

    public String getPort() {
        return port;
    }

    public List<ServiceIfaceInfo> getServiceIfaceInfos() {
        return serviceIfaceInfos;
    }

    public String getStatus() {
        return status;
    }

    public AuthInfo getAuthInfo() {
        return authInfo;
    }
}
