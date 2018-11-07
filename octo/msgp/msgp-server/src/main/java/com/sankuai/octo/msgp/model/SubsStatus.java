package com.sankuai.octo.msgp.model;

/**
 * Created by nero on 2018/5/24
 */
public class SubsStatus {

    private String appkey;
    private String owners;
    private int isReportSubs;
    private int isNodeTriggerSubs;
    private int isPerfTriggerSubs;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getOwners() {
        return owners;
    }

    public void setOwners(String owners) {
        this.owners = owners;
    }

    public int getIsReportSubs() {
        return isReportSubs;
    }

    public void setIsReportSubs(int isReportSubs) {
        this.isReportSubs = isReportSubs;
    }

    public int getIsNodeTriggerSubs() {
        return isNodeTriggerSubs;
    }

    public void setIsNodeTriggerSubs(int isNodeTriggerSubs) {
        this.isNodeTriggerSubs = isNodeTriggerSubs;
    }

    public int getIsPerfTriggerSubs() {
        return isPerfTriggerSubs;
    }

    public void setIsPerfTriggerSubs(int isPerfTriggerSubs) {
        this.isPerfTriggerSubs = isPerfTriggerSubs;
    }

    @Override
    public String toString() {
        return "SubsStatus{" +
                "appkey='" + appkey + '\'' +
                ", owners='" + owners + '\'' +
                ", isReportSubs=" + isReportSubs +
                ", isNodeTriggerSubs=" + isNodeTriggerSubs +
                ", isPerfTriggerSubs=" + isPerfTriggerSubs +
                '}';
    }
}
