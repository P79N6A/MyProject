package com.sankuai.octo.msgp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * Created by zava on 16/8/31.
 */
public class AppkeyUser {

    private String username;
    private String appkey;
    private List<String> owners;

    public AppkeyUser() {

    }

    public AppkeyUser(String username, String appkey, List<String> owners) {
        this.username = username;
        this.appkey = appkey;
        this.owners = owners;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
