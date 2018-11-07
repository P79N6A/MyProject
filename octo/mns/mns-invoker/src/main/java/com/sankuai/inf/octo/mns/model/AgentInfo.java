package com.sankuai.inf.octo.mns.model;

public class AgentInfo {

    private String ip = "";
    private int port;
    private String appkey = "";
    private String env = "";

    public AgentInfo(String ip, int port, String appkey, String env) {
        this.ip = ip;
        this.port = port;
        this.appkey = appkey;
        this.env = env;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
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

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    @Override
    public String toString() {
        return "AgentInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", appkey='" + appkey + '\'' +
                ", env='" + env + '\'' +
                '}';
    }
}
