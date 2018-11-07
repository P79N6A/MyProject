package com.sankuai.octo.statistic.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by yves on 17/4/20.
 */
public class TimeGroupKey {

    private Integer timeSeq;
    private GroupKey groupKey;
    private InstanceKey2 instanceKey;

    public TimeGroupKey(Integer timeSeq, GroupKey groupKey, InstanceKey2 instanceKey) {
        this.timeSeq = timeSeq;
        this.groupKey = groupKey;
        this.instanceKey = instanceKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeGroupKey)) return false;

        TimeGroupKey that = (TimeGroupKey) o;

        if (!timeSeq.equals(that.timeSeq)) return false;
        if (!instanceKey.equals(that.instanceKey)) return false;
        return groupKey.equals(that.groupKey);

    }

    @Override
    public int hashCode() {
        int result = timeSeq.hashCode();
        result = 31 * result + groupKey.hashCode();
        result = 31 * result + instanceKey.hashCode();
        return result;
    }


    public GroupKey getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(GroupKey groupKey) {
        this.groupKey = groupKey;
    }

    public Integer getTimeSeq() {
        return timeSeq;
    }

    public void setTimeSeq(int timeSeq) {
        this.timeSeq = timeSeq;
    }

    public void setTimeSeq(Integer timeSeq) {
        this.timeSeq = timeSeq;
    }

    public InstanceKey2 getInstanceKey() {
        return instanceKey;
    }

    public void setInstanceKey(InstanceKey2 instanceKey) {
        this.instanceKey = instanceKey;
    }


    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
