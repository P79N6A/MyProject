package com.meituan.service.mobile.mtthrift.netty.metadata;

import java.util.Arrays;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-6
 * Time: 下午5:22
 */
public class RpcResponse {

    private Class<?> serviceInterface;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;

    private Object returnVal;
    private Exception exception;
    private long seq;
    private int responseSize;

    public RpcResponse() {
    }

    public RpcResponse(Class<?> serviceInterface, String methodName, Object[] parameters) {
        this.serviceInterface = serviceInterface;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public RpcResponse(RpcRequest request) {
        checkNotNull(request, "request");
        if (request == null) {
            throw new NullPointerException("request");
        }
        this.serviceInterface = request.getServiceInterface();
        this.methodName = request.getMethodName();
        this.parameters = request.getParameters();
        this.parameterTypes = request.getParameterTypes();
        this.seq = request.getSeq();
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object getReturnVal() {
        return returnVal;
    }

    public void setReturnVal(Object returnVal) {
        this.returnVal = returnVal;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public int getResponseSize() {
        return responseSize;
    }

    public void setResponseSize(int responseSize) {
        this.responseSize = responseSize;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "RpcResponse{" + "serviceInterface=" + serviceInterface + ", methodName='" + methodName + '\''
                + ", parameters=" + Arrays.toString(parameters) + ", parameterTypes=" + Arrays.toString(parameterTypes)
                + ", returnVal=" + returnVal + ", exception=" + exception + ", seq=" + seq + ", responseSize="
                + responseSize + '}';
    }
}
