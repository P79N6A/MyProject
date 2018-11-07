package com.sankuai.meituan.config.model;

import org.apache.zookeeper.data.Stat;

public class PathStat {
    private int nodeDataVersion;
    private Long pathMaxModifiedTime;
    private Long nodeCreateTime;
    private Long pathMaxMzxid;

    public PathStat(Stat stat) {
        this.nodeDataVersion = stat.getVersion();
        this.pathMaxModifiedTime = stat.getMtime();
        this.nodeCreateTime = stat.getCtime();
        this.pathMaxMzxid = stat.getMzxid();
    }

    public void update(Stat stat) {
        this.pathMaxModifiedTime = Math.max(this.pathMaxModifiedTime, stat.getMtime());
        this.pathMaxMzxid = Math.max(this.pathMaxMzxid, stat.getMzxid());
    }

    public int getNodeDataVersion() {
        return nodeDataVersion;
    }

    public Long getNodeCreateTime() {
        return nodeCreateTime;
    }

    public Long getPathMaxModifiedTime() {
        return pathMaxModifiedTime;
    }

    public Long getPathMaxMzxid() {
        return pathMaxMzxid;
    }
}
