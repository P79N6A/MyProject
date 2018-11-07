package com.sankuai.octo.statistic.model;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.io.Serializable;
import java.util.List;

@ThriftStruct
public class Metric implements Serializable {
    @ThriftField(1)
    public MetricKey key;
    @ThriftField(2)
    public List<MetricData> data;

    @ThriftConstructor
    public Metric(MetricKey key, List<MetricData> data) {
        this.key = key;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metric)) return false;

        Metric metric = (Metric) o;

        if (data != null ? !data.equals(metric.data) : metric.data != null) return false;
        if (key != null ? !key.equals(metric.key) : metric.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "key=" + key +
                ", data=" + data +
                '}';
    }
}
