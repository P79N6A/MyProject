package com.meituan.mtrace.query;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangzhitong
 * @created 9/23/15
 */
public class TraceSummary {
    private long traceId;
    private long startTs;
    private long duration;
    private int depth;
    private Map<String ,Integer> serviceCounts = new HashMap<String, Integer>();

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public long getStartTs() {
        return startTs;
    }

    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Map<String, Integer> getServiceCounts() {
        return serviceCounts;
    }
    public void addServiceCount(String spanName) {
        Integer count = serviceCounts.get(spanName);
        if (count == null) {
            serviceCounts.put(spanName, 1);
        } else {
            serviceCounts.put(spanName, count + 1);
        }
    }
    public String toString() {
        return "TraceSummery(TraceId:" + this.traceId +
                ", startTs:" + this.startTs +
                ", duration:" + this.duration +
                ", depth:" + this.depth +
                ", serviceCounts" + this.serviceCounts +
                ")";
    }

}
