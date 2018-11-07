package com.sankuai.octo.msgp.domain;

import java.util.ArrayList;
import java.util.List;

public class AppkeyIps {
    private String appkey;
    private String env;
    private List<String> ips = new ArrayList<>();

    public AppkeyIps(){

    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
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
}
