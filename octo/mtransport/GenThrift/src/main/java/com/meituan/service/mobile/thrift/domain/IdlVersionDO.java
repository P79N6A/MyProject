package com.meituan.service.mobile.thrift.domain;

import org.springframework.stereotype.Component;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-3-6
 * Time: 下午3:03
 */
@Component
public class IdlVersionDO {

    private String appkey;
    private String uid;
    private int version;
    private String file;
    private String remark;
    private String content;

    @Override
    public String toString() {
        return "IdlVersionDO{" +
                ", appkey='" + appkey + '\'' +
                ", uid='" + uid + '\'' +
                ", version=" + version +
                ", file='" + file + '\'' +
                ", remark='" + remark + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
