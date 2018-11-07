package com.meituan.mtrace;

public class Annotation {

    private String value;
    private long timestamp;
    private int duration;

    public Annotation(String value, long timestamp, int duration) {
        this.value = value;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getDuration() {
        return duration;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Annotation(");
        sb.append("value:");
        sb.append(value);
        sb.append(", timestamp:");
        sb.append(timestamp);
        sb.append(", duration:");
        sb.append(duration);
        sb.append(")");
        return sb.toString();
    }
}
