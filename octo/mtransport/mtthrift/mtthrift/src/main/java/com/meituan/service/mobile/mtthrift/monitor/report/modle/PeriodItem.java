package com.meituan.service.mobile.mtthrift.monitor.report.modle;

import java.io.Serializable;

/**
 * User: YangXuehua
 * Date: 14-5-20
 * Time: 下午2:45
 */
@Deprecated
public class PeriodItem implements Serializable {
    private static final long serialVersionUID = -1L;

    private String serverAppKey;
    private String serverIp;
    private int serverPort;

    private String methodName;
    private int normalTimes;
    private int exceptionTimes;
    private int execAvgMills;
    private long timestamp;
    private String clusterManager;

    public String getServerAppKey() {
        return serverAppKey;
    }

    public void setServerAppKey(String serverAppKey) {
        this.serverAppKey = serverAppKey;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getNormalTimes() {
        return normalTimes;
    }

    public void setNormalTimes(int normalTimes) {
        this.normalTimes = normalTimes;
    }

    public int getExceptionTimes() {
        return exceptionTimes;
    }

    public void setExceptionTimes(int exceptionTimes) {
        this.exceptionTimes = exceptionTimes;
    }

    public int getExecAvgMills() {
        return execAvgMills;
    }

    public void setExecAvgMills(int execAvgMills) {
        this.execAvgMills = execAvgMills;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PeriodItem{" +
                "serverAppKey='" + serverAppKey + '\'' +
                ", serverIp='" + serverIp + '\'' +
                ", serverPort=" + serverPort +
                ", methodName='" + methodName + '\'' +
                ", normalTimes=" + normalTimes +
                ", exceptionTimes=" + exceptionTimes +
                ", execAvgMills=" + execAvgMills +
                ", timestamp=" + timestamp +
                '}';
    }

    public String getClusterManager() {
        return clusterManager;
    }

    public void setClusterManager(String clusterManager) {
        this.clusterManager = clusterManager;
    }
}
