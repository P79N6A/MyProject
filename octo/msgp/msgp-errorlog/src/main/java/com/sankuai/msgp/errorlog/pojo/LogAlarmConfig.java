package com.sankuai.msgp.errorlog.pojo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.sql.Timestamp;

public class LogAlarmConfig implements Serializable {
    private Integer id;

    private String appkey;

    private String alarmVirtualNode;

    private String trapper;

    private Integer gapSeconds;

    private Boolean enabled;

    private String taskOperType;

    private Timestamp updateTime;

    private static final long serialVersionUID = 1L;

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
        this.alarmVirtualNode = alarmVirtualNode;
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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        LogAlarmConfig other = (LogAlarmConfig) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAppkey() == null ? other.getAppkey() == null : this.getAppkey().equals(other.getAppkey()))
            && (this.getAlarmVirtualNode() == null ? other.getAlarmVirtualNode() == null : this.getAlarmVirtualNode().equals(other.getAlarmVirtualNode()))
            && (this.getTrapper() == null ? other.getTrapper() == null : this.getTrapper().equals(other.getTrapper()))
            && (this.getGapSeconds() == null ? other.getGapSeconds() == null : this.getGapSeconds().equals(other.getGapSeconds()))
            && (this.getEnabled() == null ? other.getEnabled() == null : this.getEnabled().equals(other.getEnabled()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAppkey() == null) ? 0 : getAppkey().hashCode());
        result = prime * result + ((getAlarmVirtualNode() == null) ? 0 : getAlarmVirtualNode().hashCode());
        result = prime * result + ((getTrapper() == null) ? 0 : getTrapper().hashCode());
        result = prime * result + ((getGapSeconds() == null) ? 0 : getGapSeconds().hashCode());
        result = prime * result + ((getEnabled() == null) ? 0 : getEnabled().hashCode());
        return result;
    }

    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}