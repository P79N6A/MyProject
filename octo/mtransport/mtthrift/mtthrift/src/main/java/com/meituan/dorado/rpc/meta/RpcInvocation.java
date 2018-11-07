package com.meituan.dorado.rpc.meta;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/5/18
 */
public class RpcInvocation {

    private Class<?> serviceInterface;
    private Method method;
    private Class<?>[] parameterTypes;
    private Object[] arguments;
    private final Map<String, Object> attachments = new HashMap<String, Object>();

    public RpcInvocation(Class<?> serviceInterface, Method method, Object[] arguments) {
        this.serviceInterface = serviceInterface;
        this.method = method;
        this.arguments = arguments;
        this.parameterTypes = method.getParameterTypes();
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Object getAttachment(String key) {
        return attachments.get(key);
    }

    public void putAttachments(Map<String, String> attachments) {
        this.attachments.putAll(attachments);
    }

    public void putAttachment(String key, Object value) {
        this.attachments.put(key, value);
    }

}
