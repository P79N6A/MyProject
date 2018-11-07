package com.sankuai.octo.statistic.model;

import java.io.Serializable;
import java.util.List;

public class MetricList implements Serializable {
    private static final long serialVersionUID = -5028355126052025665L;

    private String serializeType;

    private List<Metric> data;

    public MetricList(String serializeType, List<Metric> data) {
        this.serializeType = serializeType;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetricList)) return false;

        MetricList that = (MetricList) o;

        if (getSerializeType() != null ? !getSerializeType().equals(that.getSerializeType()) : that.getSerializeType() != null)
            return false;
        return getData() != null ? getData().equals(that.getData()) : that.getData() == null;

    }

    @Override
    public int hashCode() {
        int result = getSerializeType() != null ? getSerializeType().hashCode() : 0;
        result = 31 * result + (getData() != null ? getData().hashCode() : 0);
        return result;
    }

    public String getSerializeType() {
        return serializeType;
    }

    public void setSerializeType(String serializeType) {
        this.serializeType = serializeType;
    }

    public List<Metric> getData() {
        return data;
    }

    public void setData(List<Metric> data) {
        this.data = data;
    }
}
