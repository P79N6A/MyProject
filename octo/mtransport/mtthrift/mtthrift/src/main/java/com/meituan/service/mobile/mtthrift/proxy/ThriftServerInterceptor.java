package com.meituan.service.mobile.mtthrift.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/4/14
 * Time: 17:26
 */
public class ThriftServerInterceptor implements InvocationHandler {

    private InvocationHandler invoker;
    private List<ThriftInterceptor> interceptors;

    public ThriftServerInterceptor(InvocationHandler invoker, List<ThriftInterceptor> interceptors) {
        this.invoker = invoker;
        this.interceptors = interceptors;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        InvokerContext context = new InvokerContext(method, args);

        for (ThriftInterceptor interceptor : interceptors) {
            interceptor.beforeInvoker(context);
        }

        Object ret = null;

        try {
            ret = invoker.invoke(o, method, args);
        } catch (Throwable e) {
            context.setThrowable(e);

            for (ThriftInterceptor interceptor : interceptors) {
                interceptor.afterThrowing(context);
            }
            throw e;
        }

        context.setReturnVal(ret);

        for (ThriftInterceptor interceptor : interceptors) {
            interceptor.afterInvoker(context);
        }

        return ret;
    }
}
