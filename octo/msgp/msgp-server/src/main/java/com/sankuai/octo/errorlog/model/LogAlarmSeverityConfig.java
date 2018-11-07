package com.sankuai.octo.errorlog.model;

public class LogAlarmSeverityConfig {
    private Integer id;

    private String appkey;

    private Integer ok;

    private Integer warning;

    private Integer error;

    private Integer disaster;

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
}