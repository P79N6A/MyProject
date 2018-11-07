package com.sankuai.inf.octo.mns.falcon;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-29
 * Time: 下午2:56
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
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
