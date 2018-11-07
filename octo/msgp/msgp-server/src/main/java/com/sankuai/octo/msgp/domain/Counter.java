package com.sankuai.octo.msgp.domain;

/**
 * Created by yves on 16/12/6.
 * 用于Falcon上报
 */
public class Counter {
    private String metric;
    private String tags;
    private long time;
    private String value;

    public Counter() {
    }

    public Counter(String metric, String tags, long time, String value) {
        this.metric = metric;
        this.tags = tags;
        this.time = time;
        this.value = value;
    }

    public String getMetric() {
        return this.metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
