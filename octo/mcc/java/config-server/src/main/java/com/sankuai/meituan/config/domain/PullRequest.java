package com.sankuai.meituan.config.domain;

import java.util.Date;

public class PullRequest {
    private Integer prId;

    private String note;

    private String prMisid;

    private Integer status;

    private String appkey;

    private Integer env;

    private Date prTime;

    public Integer getPrId() {
        return prId;
    }

    public void setPrId(Integer prId) {
        this.prId = prId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
    }

    public String getPrMisid() {
        return prMisid;
    }

    public void setPrMisid(String prMisid) {
        this.prMisid = prMisid == null ? null : prMisid.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public Integer getEnv() {
        return env;
    }

    public void setEnv(Integer env) {
        this.env = env;
    }

    public Date getPrTime() {
        return prTime;
    }

    public void setPrTime(Date prTime) {
        this.prTime = prTime;
    }
}