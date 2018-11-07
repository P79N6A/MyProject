package com.sankuai.msgp.errorlog.entity;


public class ErrorLogStatisticQuery {
    private String appkey;
    private Integer stime;
    private Integer etime;
    private Integer time;
    private String host;
    private Integer filterId;
    private String exceptionName;

    public ErrorLogStatisticQuery() {

    }

    public ErrorLogStatisticQuery(String appkey, Integer stime, Integer etime) {
        this.appkey = appkey;
        this.stime = stime;
        this.etime = etime;
    }

    public ErrorLogStatisticQuery(String appkey, Integer stime, Integer etime, String host) {
        this(appkey, stime, etime);
        this.host = host;
    }


    public ErrorLogStatisticQuery(String appkey, Integer stime, Integer etime, Integer filterId, String exceptionName) {
        this(appkey, stime, etime);
        this.filterId = filterId;
        this.exceptionName = exceptionName;
    }

    public ErrorLogStatisticQuery(String appkey, Integer stime, Integer etime, String host, Integer filterId, String exceptionName) {
        this(appkey, stime, etime, filterId, exceptionName);
        this.host = host;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public Integer getStime() {
        return stime;
    }

    public void setStime(Integer stime) {
        this.stime = stime;
    }

    public Integer getEtime() {
        return etime;
    }

    public void setEtime(Integer etime) {
        this.etime = etime;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
        this.exceptionName = exceptionName;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }
}
