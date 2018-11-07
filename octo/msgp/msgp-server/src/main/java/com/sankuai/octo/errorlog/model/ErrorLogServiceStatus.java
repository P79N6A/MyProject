package com.sankuai.octo.errorlog.model;

import java.sql.Timestamp;

public class ErrorLogServiceStatus {

    public enum ServiceStatus {
        UNKNOWN("UNKNOWN"), START("START"), STOP("STOP");

        private String status;

        ServiceStatus(String status) {
            this.status = status;
        }

        public ServiceStatus toStatus(String status) {
            if ("START".equals(status)) {
                return START;
            } else if ("STOP".equals(status)) {
                return STOP;
            } else {
                return UNKNOWN;
            }
        }
    }

    private Integer id;

    private String appkey;

    private String serviceStatus;

    private Timestamp updateTime;

    public ErrorLogServiceStatus() {
    }

    public ErrorLogServiceStatus(String appkey, String serviceStatus) {
        this.appkey = appkey;
        this.serviceStatus = serviceStatus;
    }

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
        this.appkey = appkey;
    }

    public String getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
