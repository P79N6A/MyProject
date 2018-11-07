package com.sankuai.octo.scanner.model;

import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-9-25
 * Time: 下午5:26
 */
public class ProviderZkPath {

    private String version;
    private List<String> providerNameList;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getProviderNameList() {
        return providerNameList;
    }

    public void setProviderNameList(List<String> providerNameList) {
        this.providerNameList = providerNameList;
    }

    public ProviderZkPath(String version, List<String> providerNameList) {
        this.version = version;
        this.providerNameList = providerNameList;
    }

    public ProviderZkPath() {
    }
}
