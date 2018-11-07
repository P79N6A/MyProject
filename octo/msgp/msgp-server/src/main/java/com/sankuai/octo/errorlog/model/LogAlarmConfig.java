package com.sankuai.octo.errorlog.model;

import java.sql.Timestamp;

public class LogAlarmConfig {
    private Integer id;

    private String appkey;

    private String alarmVirtualNode;

    private String trapper;

    private Integer gapSeconds;

    private Boolean enabled;

    private String taskOperType;

    private Timestamp updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public String getAlarmVirtualNode() {
        return alarmVirtualNode;
    }

    public void setAlarmVirtualNode(String alarmVirtualNode) {
        this.alarmVirtualNode = alarmVirtualNode == null ? null : alarmVirtualNode.trim();
    }

    public String getTrapper() {
        return trapper;
    }

    public void setTrapper(String trapper) {
        this.trapper = trapper == null ? null : trapper.trim();
    }

    public Integer getGapSeconds() {
        return gapSeconds;
    }

    public void setGapSeconds(Integer gapSeconds) {
        this.gapSeconds = gapSeconds;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getTaskOperType() {
        return taskOperType;
    }

    public void setTaskOperType(String taskOperType) {
        this.taskOperType = taskOperType;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}