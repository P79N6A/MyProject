package com.meituan.service.mobile.mtthrift.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/4/14
 * Time: 17:24
 */
public class InvokerContext {

    private Method method;
    private Object[] args;
    private Object returnVal;
    private Throwable throwable;
    private Map attributeMap = new HashMap();

    public InvokerContext() {
    }

    public InvokerContext(Method method, Object[] args) {
        this.method = method;
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getReturnVal() {
        return returnVal;
    }

    public void setReturnVal(Object returnVal) {
        this.returnVal = returnVal;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Object getAttribute(Object key) {
        return attributeMap.get(key);
    }

    public void setAttribute(Object key, Object value) {
        this.attributeMap.put(key, value);
    }

    @Override
    public String toString() {
        return "InvokerContext{" + "method=" + method + ", args=" + Arrays.toString(args) + ", returnVal=" + returnVal
                + ", throwable=" + throwable + '}';
    }

}
