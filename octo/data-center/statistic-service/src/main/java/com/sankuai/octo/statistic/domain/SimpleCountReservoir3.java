package com.sankuai.octo.statistic.domain;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.util.HashMap;
import java.util.Map;

@ThriftStruct
public class SimpleCountReservoir3 {
    @ThriftField(1)
    public int max;
    @ThriftField(2)
    public Map<Integer, Long> values;

    @ThriftConstructor
    public SimpleCountReservoir3(int max,Map<Integer, Long> values) {
        this.max = max;
        this.values = values;
    }


    public int getMax() {
        return max;
    }

    public Map<Integer, Long> getValues() {
        return values;
    }
}
