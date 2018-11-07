package com.meituan.service.mobile.mtthrift.client.rhino;

import com.meituan.service.mobile.mtthrift.client.model.Server;
import org.aopalliance.intercept.MethodInvocation;

public class RhinoContext {
    private String localAppkey;
    private String remoteAppkey;
    private MethodInvocation methodInvocation;
    private Server remoteServer;
    private Class<?> serviceInterface;
    private int originalTimeout;
    private int currentTimeout;
    private Exception injectedException;
    private boolean delay;
    private boolean async;
    private int delayTime;

    public String getLocalAppkey() {
        return localAppkey;
    }

    public void setLocalAppkey(String localAppkey) {
        this.localAppkey = localAppkey;
    }

    public String getRemoteAppkey() {
        return remoteAppkey;
    }

    public void setRemoteAppkey(String remoteAppkey) {
        this.remoteAppkey = remoteAppkey;
    }

    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public void setMethodInvocation(MethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    public Server getRemoteServer() {
        return remoteServer;
    }

    public void setRemoteServer(Server remoteServer) {
        this.remoteServer = remoteServer;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public int getOriginalTimeout() {
        return originalTimeout;
    }

    public void setOriginalTimeout(int originalTimeout) {
        this.originalTimeout = originalTimeout;
    }

    public int getCurrentTimeout() {
        return currentTimeout;
    }

    public void setCurrentTimeout(int currentTimeout) {
        this.currentTimeout = currentTimeout;
    }

    public Exception getInjectedException() {
        return injectedException;
    }

    public void setInjectedException(Exception injectedException) {
        this.injectedException = injectedException;
    }

    public boolean isDelay() {
        return delay;
    }

    public void setDelay(boolean delay) {
        this.delay = delay;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public void throwExceptionIfPresented() throws Exception {
        if (injectedException != null) {
            throw injectedException;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RhinoContext{");
        sb.append("localAppkey='").append(localAppkey).append('\'');
        sb.append(", remoteAppkey='").append(remoteAppkey).append('\'');
        sb.append(", methodInvocation=").append(methodInvocation);
        sb.append(", remoteServer=").append(remoteServer);
        sb.append(", serviceInterface=").append(serviceInterface);
        sb.append(", originalTimeout=").append(originalTimeout);
        sb.append(", currentTimeout=").append(currentTimeout);
        sb.append(", injectedException=").append(injectedException);
        sb.append(", delay=").append(delay);
        sb.append(", async=").append(async);
        sb.append(", delayTime=").append(delayTime);
        sb.append('}');
        return sb.toString();
    }
}
