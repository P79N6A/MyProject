package com.sankuai.msgp.errorlog.pojo;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ErrorLogCount implements Comparable<ErrorLogCount>{
    private String appkey;
    private Integer logCount;

    public ErrorLogCount() {

    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public Integer getLogCount() {
        return logCount;
    }

    public void setLogCount(Integer logCount) {
        this.logCount = logCount;
    }

    public String toString(){
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public int compareTo(ErrorLogCount o) {
        return this.getLogCount().compareTo(o.getLogCount());
    }
}
