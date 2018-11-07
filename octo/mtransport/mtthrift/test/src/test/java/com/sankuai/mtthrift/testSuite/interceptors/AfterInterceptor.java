package com.sankuai.mtthrift.testSuite.interceptors;

import com.meituan.service.mobile.mtthrift.proxy.InvokerContext;
import com.meituan.service.mobile.mtthrift.proxy.ThriftInterceptor;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/4/17
 * Time: 11:31
 */
public class AfterInterceptor implements ThriftInterceptor {
    @Override
    public void beforeInvoker(InvokerContext invokerContext) {

    }

    @Override
    public void afterInvoker(InvokerContext invokerContext) {
        System.out.println("after: " + invokerContext);
    }

    @Override
    public void afterThrowing(InvokerContext invokerContext) {

    }
}
