package com.meituan.service.mobile.mtthrift.proxy;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/4/14
 * Time: 17:24
 */
public interface ThriftInterceptor {
    void beforeInvoker(InvokerContext invokerContext);

    void afterInvoker(InvokerContext invokerContext);

    void afterThrowing(InvokerContext invokerContext);
}
