package com.sankuai.octo.msgp.model.flowCopy;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/9/13
 * Time: 16:03
 */
public class FlowCopyPtestConfig {
    private String misId;
    private String appName;//应用名
    private String serviceName;//服务名
    private String methodNames;//方法名（不带参数，重载方法全部录制）集合，多个方法逗号（,）分隔
    private Long sumCount;//录制的条数，总条数
    private String serverIps;//指定录制的服务器ip集合，多个ip逗号（,）分隔
    private String savePath;//S3文件路径，文件名：rpc类型_服务名_时间戳.tf
    private boolean tagged;//是否染色，默认true，染色指的是context中带压测tag
    private String description;//备注信息

    public String getMisId() {
        return misId;
    }

    public void setMisId(String misId) {
        this.misId = misId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodNames() {
        return methodNames;
    }

    public void setMethodNames(String methodNames) {
        this.methodNames = methodNames;
    }

    public Long getSumCount() {
        return sumCount;
    }

    public void setSumCount(Long sumCount) {
        this.sumCount = sumCount;
    }

    public String getServerIps() {
        return serverIps;
    }

    public void setServerIps(String serverIps) {
        this.serverIps = serverIps;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public boolean isTagged() {
        return tagged;
    }

    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlowCopyPtestConfig{");
        sb.append("misId='").append(misId).append('\'');
        sb.append(", appName='").append(appName).append('\'');
        sb.append(", serviceName='").append(serviceName).append('\'');
        sb.append(", methodNames='").append(methodNames).append('\'');
        sb.append(", sumCount=").append(sumCount);
        sb.append(", serverIps='").append(serverIps).append('\'');
        sb.append(", savePath='").append(savePath).append('\'');
        sb.append(", tagged=").append(tagged);
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}