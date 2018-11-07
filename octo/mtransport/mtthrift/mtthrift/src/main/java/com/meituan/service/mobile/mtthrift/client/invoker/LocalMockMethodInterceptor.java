package com.meituan.service.mobile.mtthrift.client.invoker;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-10-21
 * Time: 下午6:30
 */
public class LocalMockMethodInterceptor implements MethodInterceptor{

    private String mockServiceImpl;

    public LocalMockMethodInterceptor(String mockServiceImpl) {
        this.mockServiceImpl = mockServiceImpl;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        Object[] args = methodInvocation.getArguments();
        Object target = Class.forName(mockServiceImpl).newInstance();
        Object result = method.invoke(target, args);
        return result;
    }

    public String getMockServiceImpl() {
        return mockServiceImpl;
    }

    public void setMockServiceImpl(String mockServiceImpl) {
        this.mockServiceImpl = mockServiceImpl;
    }
}
