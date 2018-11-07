package com.sankuai.octo.statistic.model;


import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftEnumValue;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@ThriftEnum
public enum StatGroup {
    SpanLocalHost(0),
    SpanRemoteApp(1),
    Span(2),
    SpanRemoteHost(3),
    LocalHostRemoteHost(4),
    LocalHostRemoteApp(5),
    RemoteAppRemoteHost(6);

    private static final Map<String, StatGroup> nameToValueMap = new HashMap<>();

    private static final Map<Integer, StatGroup> typeIdToValueMap = new HashMap<>();

    static {
        for (StatGroup statGroup : StatGroup.values()) {
            nameToValueMap.put(statGroup.toString().toLowerCase(), statGroup);
            typeIdToValueMap.put(statGroup.type, statGroup);
        }
    }

    private int type;

    StatGroup(int type) {
        this.type = type;
    }

    public static StatGroup getInstance(String group) {
        if (StringUtils.hasText(group) && nameToValueMap.containsKey(group.toLowerCase())) {
            return nameToValueMap.get(group.toLowerCase());
        } else {
            return StatGroup.Span;
        }
    }

    public static StatGroup getInstance(Integer type) {
        if (typeIdToValueMap.containsKey(type)) {
            return typeIdToValueMap.get(type);
        } else {
            throw new IllegalArgumentException("wrong type");
        }
    }

    public int getType() {
        return type;
    }

    @ThriftEnumValue
    public int getIntValue() {
        return this.ordinal();
    }
}
