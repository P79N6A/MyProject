package com.meituan.service.mobile.thrift.model;


import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-1-30
 * Time: 上午11:32
 */

@Component
public class GenThriftForm {

    private String appkey;
    private String filename;
    private List<String> languages;
    private String thriftVersion;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public String getThriftVersion() {
        return thriftVersion;
    }

    public void setThriftVersion(String thriftVersion) {
        this.thriftVersion = thriftVersion;
    }
}
