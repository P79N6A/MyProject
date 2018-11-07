package com.sankuai.octo.statistic.model;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.meituan.mtrace.thrift.model.StatusCode;

import java.io.Serializable;

@ThriftStruct
public class MetricData implements Serializable {
    @ThriftField(1)
    public long start;
    @ThriftField(2)
    public int count;
    @ThriftField(3)
    public int cost;
    @ThriftField(4)
    public StatusCode status;

    @ThriftConstructor
    public MetricData(long start, int count, int cost, StatusCode status) {
        this.start = start;
        this.count = count;
        this.cost = cost;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricData that = (MetricData) o;

        if (cost != that.cost) return false;
        if (count != that.count) return false;
        if (start != that.start) return false;
        if (status != that.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (start ^ (start >>> 32));
        result = 31 * result + count;
        result = 31 * result + cost;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MetricData{" +
                "start=" + start +
                ", count=" + count +
                ", cost=" + cost +
                ", status=" + status +
                '}';
    }

}
