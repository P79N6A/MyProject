package com.sankuai.octo.msgp.domain;

import java.util.List;

/**
 * @author uu
 * @description
 * @date Created in 22:33 2018/5/2
 * @modified
 */
public class AppkeyAuth {
    private String appkey;
    private String env;
    private List<String> allAuthList;

    public AppkeyAuth() {
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public List<String> getAllAuthList() {
        return allAuthList;
    }

    public void setAllAuthList(List<String> allAuthList) {
        this.allAuthList = allAuthList;
    }
}
