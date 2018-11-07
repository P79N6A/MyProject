package com.meituan.service.mobile.mtthrift.client.model;

import java.io.Serializable;

/**
 * User: YangXuehua
 * Date: 13-8-12
 * Time: 下午4:11
 */
@Deprecated
public class ServerFeedback implements Serializable {
    private static final long serialVersionUID = 6121511348472568555L;

    private int getConnectSuccessNums;
    private int getConnectFailedNums;
    private int invokeTimeoutNums;
    private int invokeSuccessNums;
    private int invokeTime;
    private int score;// 相对于平均水平的评分：负数表示差于平均水平，0表示与平均水平相差不大，正数表示优于平均水平

    public ServerFeedback(int getConnectSuccessNums, int getConnectFailedNums, int invokeTimeoutNums, int invokeSuccessNums, int invokeTime) {
        this.getConnectSuccessNums = getConnectSuccessNums;
        this.getConnectFailedNums = getConnectFailedNums;
        this.invokeTimeoutNums = invokeTimeoutNums;
        this.invokeSuccessNums = invokeSuccessNums;
        this.invokeTime = invokeTime;
    }

    public int getGetConnectSuccessNums() {
        return getConnectSuccessNums;
    }

    public int getGetConnectFailedNums() {
        return getConnectFailedNums;
    }

    public int getInvokeTimeoutNums() {
        return invokeTimeoutNums;
    }

    public int getInvokeSuccessNums() {
        return invokeSuccessNums;
    }

    public int getInvokeTime() {
        return invokeTime;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getGetConnectFailedRate() {
        if (getConnectSuccessNums + getConnectFailedNums > 0)
            return 100 * getConnectFailedNums / (getConnectSuccessNums + getConnectFailedNums);
        else
            return 0;
    }

    public int getInvokeTimeoutRate() {
        if (invokeSuccessNums + invokeTimeoutNums > 0)
            return 100 * invokeTimeoutNums / (invokeSuccessNums + invokeTimeoutNums);
        else
            return 0;
    }

    @Override
    public String toString() {
        return "ServerFeedback{" + "getConnectSuccessNums=" + getConnectSuccessNums + ", getConnectFailedNums=" + getConnectFailedNums + ", invokeTimeoutNums="
                + invokeTimeoutNums + ", invokeSuccessNums=" + invokeSuccessNums + ", invokeTime=" + invokeTime + ", score=" + score + '}';
    }
}
