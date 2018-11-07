package com.sankuai.octo.msgp.domain;

/**
 * Created by zava on 16/8/5.
 */
public class IpPortHostname {

    private String id;
    private String ip;
    private String port;
    private String hostname;
    private String name;

    public IpPortHostname() {

    }
    public IpPortHostname(String id,String ip,String port,String hostname,String name) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.hostname = hostname;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
