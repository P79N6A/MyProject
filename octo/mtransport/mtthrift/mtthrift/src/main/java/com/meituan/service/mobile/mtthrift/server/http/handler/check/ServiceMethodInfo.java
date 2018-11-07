package com.meituan.service.mobile.mtthrift.server.http.handler.check;

import java.util.Set;

public class ServiceMethodInfo {

    private String serviceName;
    private Set<String> methods;

    public ServiceMethodInfo(String serviceName, Set<String> methodNames) {
        this.serviceName = serviceName;
        this.methods = methodNames;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Set<String> getMethods() {
        return methods;
    }

    public void setMethods(Set<String> methods) {
        this.methods = methods;
    }
}
