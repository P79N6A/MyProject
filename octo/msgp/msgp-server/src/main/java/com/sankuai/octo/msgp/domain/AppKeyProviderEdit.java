package com.sankuai.octo.msgp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class AppKeyProviderEdit {
    private String username;
    private String appkey;
    private int nodetype;
    private String swimlane="";

    private List<AppkeyProviderNode> nodes= new ArrayList<AppkeyProviderNode>();;

    public AppKeyProviderEdit(){

    }

    public int getNodetype() {
        return nodetype;
    }

    public void setNodetype(int nodetype) {
        this.nodetype = nodetype;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<AppkeyProviderNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<AppkeyProviderNode> nodes) {
        this.nodes = nodes;
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
