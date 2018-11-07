package com.sankuai.msgp.errorlog.pojo;


import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class ErrorLogStatistic implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer time;

    private String appkey;

    private String hostSet;

    private String host;

    private Integer filterId;

    private String exceptionName;

    private Integer count;

    private Integer duplicateCount;

    public ErrorLogStatistic() {

    }

    public ErrorLogStatistic(Long id, Integer time, String appkey, String hostSet, String host, Integer filterId, String exceptionName,
                             Integer count, Integer duplicateCount) {
        this.id = id;
        this.time = time;
        this.appkey = appkey;
        this.hostSet = hostSet;
        this.host = host;
        this.filterId = filterId;
        this.exceptionName = exceptionName;
        this.count = count;
        this.duplicateCount = duplicateCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host == null ? null : host.trim();
    }

    public Integer getFilterId() {
        return filterId;
    }

    public void setFilterId(Integer filterId) {
        this.filterId = filterId;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName == null ? null : exceptionName.trim();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(Integer duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public String getHostSet() {
        return hostSet;
    }

    public void setHostSet(String hostSet) {
        this.hostSet = hostSet;
    }

    public void updateCount(Integer count, Integer duplicateCount) {
        this.count += count;
        this.duplicateCount += duplicateCount;
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
        ErrorLogStatistic other = (ErrorLogStatistic) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getTime() == null ? other.getTime() == null : this.getTime().equals(other.getTime()))
                && (this.getAppkey() == null ? other.getAppkey() == null : this.getAppkey().equals(other.getAppkey()))
                && (this.getHost() == null ? other.getHost() == null : this.getHost().equals(other.getHost()))
                && (this.getFilterId() == null ? other.getFilterId() == null : this.getFilterId().equals(other.getFilterId()))
                && (this.getExceptionName() == null ? other.getExceptionName() == null : this.getExceptionName().equals(other.getExceptionName()))
                && (this.getHostSet() == null ? other.getHostSet() == null : this.getHostSet().equals(other.getHostSet()));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(appkey)
                .append(host)
                .append(filterId)
                .append(exceptionName)
                .append(time)
                .toHashCode();

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ErrorLogStatistic{")
                .append("id=").append(id).append(", ")
                .append("time=").append(time).append(", ")
                .append("appkey=").append(appkey).append(", ")
                .append("hostSet=").append(hostSet).append(", ")
                .append("host=").append(host).append(", ")
                .append("filterId=").append(filterId).append(", ")
                .append("exceptionName=").append(exceptionName).append(", ")
                .append("count=").append(count).append(", ")
                .append("duplicateCount=").append(duplicateCount).append(", ");
        return sb.toString();
    }

}