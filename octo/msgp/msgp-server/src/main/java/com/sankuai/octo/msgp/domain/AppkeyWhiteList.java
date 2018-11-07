package com.sankuai.octo.msgp.domain;
import java.util.List;
/**
 * @author uu
 * @description
 * @date Created in 19:51 2018/5/2
 * @modified
 */
public class AppkeyWhiteList {
    private String appkey;
    private String env;
    private List<String> whitelist;

    public AppkeyWhiteList() {
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

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

}