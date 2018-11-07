package com.meituan.service.mobile.mtthrift.netty.metadata;

import com.google.common.util.concurrent.SettableFuture;
import com.meituan.service.mobile.mtthrift.annotation.ThriftMethodHandler;
import org.apache.thrift.async.AsyncMethodCallback;

import java.util.Arrays;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-6
 * Time: 下午3:36
 */
public class RpcRequest {
    private final Object lock = new Object();
    private Class<?> serviceInterface;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;
    private long seq;
    private long startMillis;
    private long timeoutMillis;
    private int requestSize;
    private boolean annotatedThrift;
    private ThriftMethodHandler thriftMethodHandler;
    private boolean async;
    private AsyncMethodCallback callback;
    private SettableFuture<Object> future;
    private boolean uniProto;
    private int annoSeq;
    private byte[] requestBytes;

    public RpcRequest() {
    }

    public RpcRequest(Class<?> serviceInterface, String methodName, Object[] parameters) {
        this.serviceInterface = serviceInterface;
        this.methodName = methodName;
        this.parameters = parameters;
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

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public int getRequestSize() {
        return requestSize;
    }

    public void setRequestSize(int requestSize) {
        this.requestSize = requestSize;
    }

    public boolean isAnnotatedThrift() {
        return annotatedThrift;
    }

    public ThriftMethodHandler getThriftMethodHandler() {
        return thriftMethodHandler;
    }

    public void setThriftMethodHandler(ThriftMethodHandler thriftMethodHandler) {
        this.thriftMethodHandler = thriftMethodHandler;
        this.annotatedThrift = true;
    }

    public AsyncMethodCallback getCallback() {
        return callback;
    }

    public void setCallback(AsyncMethodCallback callback) {
        this.callback = callback;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public boolean isAsync() {
        return async;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(long startMillis) {
        this.startMillis = startMillis;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public SettableFuture<Object> getFuture() {
        return future;
    }

    public void setFuture(SettableFuture future) {
        this.future = future;
    }

    public void setAnnotatedThrift(boolean annotatedThrift) {
        this.annotatedThrift = annotatedThrift;
    }

    public boolean isUniProto() {
        return uniProto;
    }

    public void setUniProto(boolean uniProto) {
        this.uniProto = uniProto;
    }

    public int getAnnoSeq() {
        return annoSeq;
    }

    public void setAnnoSeq(int annoSeq) {
        this.annoSeq = annoSeq;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RpcRequest{");
        sb.append("serviceInterface=").append(serviceInterface);
        sb.append(", methodName='").append(methodName).append('\'');
        sb.append(", parameters=").append(Arrays.toString(parameters));
        sb.append(", parameterTypes=").append(Arrays.toString(parameterTypes));
        sb.append(", seq=").append(seq);
        sb.append(", startMillis=").append(startMillis);
        sb.append(", timeoutMillis=").append(timeoutMillis);
        sb.append(", requestSize=").append(requestSize);
        sb.append(", annotatedThrift=").append(annotatedThrift);
        sb.append(", thriftMethodHandler=").append(thriftMethodHandler);
        sb.append(", async=").append(async);
        sb.append(", callback=").append(callback);
        sb.append(", uniProto=").append(uniProto);
        sb.append(", annoSeq=").append(annoSeq);
        sb.append('}');
        return sb.toString();
    }

    public final Object getLock() {
        return lock;
    }

    public byte[] getRequestBytes() {
        return requestBytes;
    }

    public void setRequestBytes(byte[] requestBytes) {
        this.requestBytes = requestBytes;
    }
}
