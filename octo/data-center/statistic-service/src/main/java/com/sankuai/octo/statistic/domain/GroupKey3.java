package com.sankuai.octo.statistic.domain;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.sankuai.octo.statistic.model.StatGroup;
import com.sankuai.octo.statistic.model.StatRange;

@ThriftStruct
public class GroupKey3 {
    @ThriftField(1)
    public int ts;
    @ThriftField(2)
    public StatRange range;
    @ThriftField(3)
    public StatGroup group;
    @ThriftField(4)
    public StatTag3 statTag;

    @ThriftConstructor
    public GroupKey3(int ts, StatRange range, StatGroup group, StatTag3 statTag) {
        this.ts = ts;
        this.range = range;
        this.group = group;
        this.statTag = statTag;
    }

    public int getTs() {
        return ts;
    }

    public StatRange getRange() {
        return range;
    }

    public StatGroup getGroup() {
        return group;
    }

    public StatTag3 getStatTag() {
        return statTag;
    }
}
