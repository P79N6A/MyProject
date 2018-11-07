package com.meituan.service.mobile.mtthrift.server.flow;

import java.util.List;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/1
 * Time: 15:21
 */
public class FlowCopyCfgDetail {
    private String serviceName;

    private List<String> methodNames;

    private long sumCount;

    private List<String> serverIps;

    private boolean tagged;

    private String savePath;

    private String description;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getMethodNames() {
        return methodNames;
    }

    public void setMethodNames(List<String> methodNames) {
        this.methodNames = methodNames;
    }

    public long getSumCount() {
        return sumCount;
    }

    public void setSumCount(long sumCount) {
        this.sumCount = sumCount;
    }

    public List<String> getServerIps() {
        return serverIps;
    }

    public void setServerIps(List<String> serverIps) {
        this.serverIps = serverIps;
    }

    public boolean isTagged() {
        return tagged;
    }

    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlowCopyCfgDetail{");
        sb.append("serviceName='").append(serviceName).append('\'');
        sb.append(", methodNames=").append(methodNames);
        sb.append(", sumCount=").append(sumCount);
        sb.append(", serverIps=").append(serverIps);
        sb.append(", tagged=").append(tagged);
        sb.append(", savePath='").append(savePath).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
