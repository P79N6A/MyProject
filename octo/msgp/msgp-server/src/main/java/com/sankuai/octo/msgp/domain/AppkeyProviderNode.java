package com.sankuai.octo.msgp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AppkeyProviderNode {
    private String version="original";
    private String ip;
    private int port;
    private int env  = 0;
    private int weight;
    private int status;
    private int enabled  = 0;
    private int role;
    private String swimlane="";

    public AppkeyProviderNode(){

    }
    public int getEnv() {
        return env;
    }

    public void setEnv(int env) {
        this.env = env;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    @Override
     public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
