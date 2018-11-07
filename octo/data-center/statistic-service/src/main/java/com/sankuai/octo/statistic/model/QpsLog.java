package com.sankuai.octo.statistic.model;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class QpsLog {
    private String providerAppKey;
    private String spanName;
    private String consumerAppKey;
    private int unixTime;
    private long count;

    @ThriftConstructor
    public QpsLog(String providerAppKey, String spanName, String consumerAppKey, int unixTime, long count) {
        this.providerAppKey = providerAppKey;
        this.spanName = spanName;
        this.consumerAppKey = consumerAppKey;
        this.unixTime = unixTime;
        this.count = count;
    }

    @ThriftField(1)
    public String getProviderAppKey() {
        return providerAppKey;
    }

    @ThriftField
    public void setProviderAppKey(String providerAppKey) {
        this.providerAppKey = providerAppKey;
    }

    @ThriftField(2)
    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    @ThriftField(3)
    public String getConsumerAppKey() {
        return consumerAppKey;
    }

    @ThriftField
    public void setConsumerAppKey(String consumerAppKey) {
        this.consumerAppKey = consumerAppKey;
    }

    @ThriftField(4)
    public int getUnixTime() {
        return unixTime;
    }

    @ThriftField
    public void setUnixTime(int unixTime) {
        this.unixTime = unixTime;
    }

    @ThriftField(5)
    public long getCount() {
        return count;
    }

    @ThriftField
    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QpsLog qpsLog = (QpsLog) o;

        if (unixTime != qpsLog.unixTime) return false;
        if (count != qpsLog.count) return false;
        if (providerAppKey != null ? !providerAppKey.equals(qpsLog.providerAppKey) : qpsLog.providerAppKey != null)
            return false;
        if (spanName != null ? !spanName.equals(qpsLog.spanName) : qpsLog.spanName != null) return false;
        return !(consumerAppKey != null ? !consumerAppKey.equals(qpsLog.consumerAppKey) : qpsLog.consumerAppKey != null);

    }

    @Override
    public int hashCode() {
        int result = providerAppKey != null ? providerAppKey.hashCode() : 0;
        result = 31 * result + (spanName != null ? spanName.hashCode() : 0);
        result = 31 * result + (consumerAppKey != null ? consumerAppKey.hashCode() : 0);
        result = 31 * result + unixTime;
        result = 31 * result + (int) (count ^ (count >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "QpsLog{" +
                "providerAppKey='" + providerAppKey + '\'' +
                ", spanName='" + spanName + '\'' +
                ", consumerAppKey='" + consumerAppKey + '\'' +
                ", unixTime=" + unixTime +
                ", count=" + count +
                '}';
    }
}