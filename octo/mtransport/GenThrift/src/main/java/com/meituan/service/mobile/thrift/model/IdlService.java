package com.meituan.service.mobile.thrift.model;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gaosheng
 * Date: 15-1-21
 * Time: 下午4:38
 * To change this template use File | Settings | File Templates.
 */
public class IdlService {

    private String serviceName;
    private List<IdlMethod> methodList;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<IdlMethod> getMethodList() {
        return methodList;
    }

    public void setMethodList(List<IdlMethod> methodList) {
        this.methodList = methodList;
    }
}
