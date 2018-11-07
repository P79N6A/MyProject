package com.sankuai.octo.msgp.domain;

import java.util.List;

public class IpAppkeys {
    private String ip;
    private List<String> appkeys;

    public IpAppkeys(){

    }

    public IpAppkeys(String ip,List<String> appkeys){
        this.ip = ip;
        this.appkeys = appkeys;
    }
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public List<String> getAppkeys() {
        return appkeys;
    }

    public void setAppkeys(List<String> appkeys) {
        this.appkeys = appkeys;
    }
}
