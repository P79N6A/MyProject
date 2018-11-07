package com.sankuai.msgp.errorlog.pojo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class LogAlarmSeverityConfig implements Serializable {
    private Integer id;

    private String appkey;

    private Integer ok;

    private Integer warning;

    private Integer error;

    private Integer disaster;

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

    public Integer getOk() {
        return ok;
    }

    public void setOk(Integer ok) {
        this.ok = ok;
    }

    public Integer getWarning() {
        return warning;
    }

    public void setWarning(Integer warning) {
        this.warning = warning;
    }

    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public Integer getDisaster() {
        return disaster;
    }

    public void setDisaster(Integer disaster) {
        this.disaster = disaster;
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
        LogAlarmSeverityConfig other = (LogAlarmSeverityConfig) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAppkey() == null ? other.getAppkey() == null : this.getAppkey().equals(other.getAppkey()))
            && (this.getOk() == null ? other.getOk() == null : this.getOk().equals(other.getOk()))
            && (this.getWarning() == null ? other.getWarning() == null : this.getWarning().equals(other.getWarning()))
            && (this.getError() == null ? other.getError() == null : this.getError().equals(other.getError()))
            && (this.getDisaster() == null ? other.getDisaster() == null : this.getDisaster().equals(other.getDisaster()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAppkey() == null) ? 0 : getAppkey().hashCode());
        result = prime * result + ((getOk() == null) ? 0 : getOk().hashCode());
        result = prime * result + ((getWarning() == null) ? 0 : getWarning().hashCode());
        result = prime * result + ((getError() == null) ? 0 : getError().hashCode());
        result = prime * result + ((getDisaster() == null) ? 0 : getDisaster().hashCode());
        return result;
    }

    public String toString(){
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}