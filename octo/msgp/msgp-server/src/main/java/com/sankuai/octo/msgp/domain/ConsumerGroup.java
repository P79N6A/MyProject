package com.sankuai.octo.msgp.domain;

import java.util.ArrayList;
import java.util.List;

public class ConsumerGroup {
    private List<String> ips = new ArrayList<String>();
    private List<String> appkeys = new ArrayList<String>();
    private List<String> idcs = new ArrayList<String>();

    public ConsumerGroup() {

    }

    public List<String> getAppkeys() {
        return appkeys;
    }

    public void setAppkeys(List<String> appkeys) {
        this.appkeys = appkeys;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public List<String> getIdcs() {
        return idcs;
    }

    public void setIdcs(List<String> idcs) {
        this.idcs = idcs;
    }
}
