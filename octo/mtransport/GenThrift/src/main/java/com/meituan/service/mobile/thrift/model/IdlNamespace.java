package com.meituan.service.mobile.thrift.model;

/**
 * Created with IntelliJ IDEA.
 * User: gaosheng
 * Date: 15-1-21
 * Time: 下午3:35
 * To change this template use File | Settings | File Templates.
 */
public class IdlNamespace {
    private String language;
    private String namespace;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
