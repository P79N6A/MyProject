package com.meituan.mtrace.common;

/**
 * @author zhangzhitong
 * @created 9/24/15
 */
public class Endpoint {
    private int ip;
    private short port;
    private String appKey;

    public Endpoint(String appKey, int ip, short port) {
        this.appKey = appKey;
        this.ip = ip;
        this.port = port;

    }
    public int getIp() {
        return ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void clone(com.meituan.mtrace.thriftjava.Endpoint endpoint) {
        this.ip = endpoint.getIp();
        this.port = endpoint.getPort();
        if (endpoint.getAppKey() != null) {
            this.appKey = endpoint.getAppKey();
        }
    }
    public String toString() {
        return "Endpoint(ip:" + this.ip + ", port:"  + this.port + ", appKey:" + this.appKey + ")";
    }
}
