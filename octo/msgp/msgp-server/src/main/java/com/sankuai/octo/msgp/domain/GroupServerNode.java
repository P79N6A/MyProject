package com.sankuai.octo.msgp.domain;

public class GroupServerNode {
    private String ip;
    private int port;

    public GroupServerNode(){

    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
