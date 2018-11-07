package com.sankuai.octo.statistic.domain;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * Instance2的 java版本
 */
@ThriftStruct
public class Instance3 {
    @ThriftField(1)
    public InstanceKey3 key;
    @ThriftField(2)
    public GroupKey3 groupKey;
    @ThriftField(3)
    public  SimpleCountHistogram3 histogram;
    @ThriftConstructor
    public Instance3(InstanceKey3 key, GroupKey3 groupKey, SimpleCountHistogram3 histogram) {
        this.key = key;
        this.groupKey = groupKey;
        this.histogram = histogram;
    }

    public InstanceKey3 getKey() {
        return key;
    }

    public GroupKey3 getGroupKey() {
        return groupKey;
    }

    public SimpleCountHistogram3 getHistogram() {
        return histogram;
    }
}
