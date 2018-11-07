package com.sankuai.msgp.errorlog.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * appkey ,是异常日志时间
 */
public class AppErrorLog {
    private String appkey;
    private int time;


    public AppErrorLog() {

    }

    public AppErrorLog(String appkey, int time) {
        this.appkey = appkey;
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }


    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(appkey)
                .append(time)
                .toHashCode();
    }

    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
