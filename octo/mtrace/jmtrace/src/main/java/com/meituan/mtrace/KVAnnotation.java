package com.meituan.mtrace;

public class KVAnnotation {
    private String key;
    private String value;

    public KVAnnotation(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("KVAnnotation(");
        sb.append("key:");
        sb.append(key);
        sb.append(", value:");
        sb.append(value);
        sb.append(")");
        return sb.toString();
    }
}
