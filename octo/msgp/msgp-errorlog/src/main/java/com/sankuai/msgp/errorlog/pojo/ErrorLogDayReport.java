package com.sankuai.msgp.errorlog.pojo;

import java.io.Serializable;
import java.util.Date;

public class ErrorLogDayReport implements Serializable {
    private Long id;

    private String appkey;

    private Date dt;

    private Integer logCount;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }

    public Integer getLogCount() {
        return logCount;
    }

    public void setLogCount(Integer logCount) {
        this.logCount = logCount;
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
        ErrorLogDayReport other = (ErrorLogDayReport) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAppkey() == null ? other.getAppkey() == null : this.getAppkey().equals(other.getAppkey()))
            && (this.getDt() == null ? other.getDt() == null : this.getDt().equals(other.getDt()))
            && (this.getLogCount() == null ? other.getLogCount() == null : this.getLogCount().equals(other.getLogCount()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAppkey() == null) ? 0 : getAppkey().hashCode());
        result = prime * result + ((getDt() == null) ? 0 : getDt().hashCode());
        result = prime * result + ((getLogCount() == null) ? 0 : getLogCount().hashCode());
        return result;
    }
}