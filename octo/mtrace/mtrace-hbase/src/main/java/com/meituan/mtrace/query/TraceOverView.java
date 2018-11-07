package com.meituan.mtrace.query;

import java.util.Map;

/**
 * @author zhangzhitong
 * @created 9/25/15
 */
public class TraceOverView {
    private long traceId;
    private int duration;
    private long startTs;
    private String rootService;
    private Map<String, ServItem> services;

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getRootService() {
        return rootService;
    }

    public void setRootService(String rootService) {
        this.rootService = rootService;
    }

    public Map<String, ServItem> getServices() {
        return services;
    }

    public void setServices(Map<String, ServItem> services) {
        this.services = services;
    }

    public long getStartTs() {
        return startTs;
    }

    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    public String toString() {
        return "TraceOverView(traceId:" + this.traceId +
                ", rootService:" + this.rootService +
                ", duration:" + this.duration +
                ", startTs:" + this.startTs +
                ", services:" + this.services +
                ")";
    }

}
