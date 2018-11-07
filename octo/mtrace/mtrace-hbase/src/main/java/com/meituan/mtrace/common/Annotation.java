package com.meituan.mtrace.common;

/**
 * @author zhangzhitong
 * @created 9/24/15
 */
public class Annotation {
    private String value;
    private long timestamp;
    private int duration;
    private Endpoint endpoint;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String toString() {
        return "Annotation(value:" + this.value + ", timestamp:" + this.timestamp + ", duration:" + this.duration + ", endpoint:" + this.endpoint + ")";
    }
}
