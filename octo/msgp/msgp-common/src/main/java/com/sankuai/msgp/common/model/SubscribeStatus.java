package com.sankuai.msgp.common.model;

/**
 * Created by yves on 17/4/21.
 */
public enum SubscribeStatus {
    not_subscribed(0, "未订阅"), subscribed(1, "已订阅");

    private int status;
    private String desc;

    private SubscribeStatus(int  status, String desc){
        this.status = status;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "SubscribeStatus{" +
                "status=" + status +
                ", desc='" + desc + '\'' +
                '}';
    }

    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
