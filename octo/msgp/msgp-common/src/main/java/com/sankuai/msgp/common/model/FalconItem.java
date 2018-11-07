package com.sankuai.msgp.common.model;

// falcon数据结构，参考wiki https://123.sankuai.com/km/page/14722249
public class FalconItem {
    private String endpoint;
    private String metric;
    private long timestamp;
    private String value;
    private String tags;

    private final String counterType = "STRING";
    private final int step = 60;

    public FalconItem(String endpoint, String metric, long timestamp, String value, String tags) {
        this.endpoint = endpoint;
        this.metric = metric;
        this.timestamp = timestamp;
        this.value = value;
        this.tags = tags;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getStep() {
        return step;
    }

    public String getCounterType() {
        return counterType;
    }
}
