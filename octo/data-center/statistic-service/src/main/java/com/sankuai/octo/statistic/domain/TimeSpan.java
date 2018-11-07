package com.sankuai.octo.statistic.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by yves on 17/4/20.
 */
public class TimeSpan {

    private Integer timeSeq;
    private String spanMetricName;

    public TimeSpan(Integer timeSeq, String spanMetricName) {
        this.timeSeq = timeSeq;
        this.spanMetricName = spanMetricName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSpan)) return false;

        TimeSpan timeSpan = (TimeSpan) o;

        if (!timeSeq.equals(timeSpan.timeSeq)) return false;
        return spanMetricName.equals(timeSpan.spanMetricName);

    }

    @Override
    public int hashCode() {
        int result = timeSeq.hashCode();
        result = 31 * result + spanMetricName.hashCode();
        return result;
    }

    public Integer getTimeSeq() {
        return timeSeq;
    }

    public String getSpanMetricName() {
        return spanMetricName;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
