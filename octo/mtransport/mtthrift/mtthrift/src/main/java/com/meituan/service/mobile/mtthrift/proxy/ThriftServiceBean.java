package com.meituan.service.mobile.mtthrift.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;


public class ThriftServiceBean {

    public Object serviceImpl;
    public Executor serviceExecutor;
    public Map<String, Executor> methodExecutor = new HashMap<String, Executor>();

    public ThriftServiceBean() {

    }

    public Object getServiceImpl() {
        return serviceImpl;
    }

    public void setServiceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public Executor getServiceExecutor() {
        return serviceExecutor;
    }

    public void setServiceExecutor(Executor serviceExecutor) {
        this.serviceExecutor = serviceExecutor;
    }

    public Map<String, Executor> getMethodExecutor() {
        return methodExecutor;
    }

    public void setMethodExecutor(Map<String, Executor> methodExecutor) {
        this.methodExecutor = methodExecutor;
    }
}
