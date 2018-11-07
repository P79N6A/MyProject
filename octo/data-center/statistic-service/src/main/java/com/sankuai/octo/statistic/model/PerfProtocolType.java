package com.sankuai.octo.statistic.model;

import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftEnumValue;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wujinwu on 15/12/1.
 */
@ThriftEnum
public enum PerfProtocolType {
    THRIFT(0),
    HTTP(1);

    private static final Map<String, PerfProtocolType> valueMap = new HashMap<>();

    private static final Map<Integer, PerfProtocolType> typeToValueMap = new HashMap<>();

    static {
        for (PerfProtocolType protocolType : PerfProtocolType.values()) {
            valueMap.put(protocolType.toString().toLowerCase(), protocolType);
            typeToValueMap.put(protocolType.getType(), protocolType);
        }
    }

    private int type;

    PerfProtocolType(int type) {
        this.type = type;
    }

    public static PerfProtocolType getInstance(String key) {
        if (StringUtils.hasText(key) && valueMap.containsKey(key.toLowerCase())) {
            return valueMap.get(key.toLowerCase());
        } else {
            return THRIFT;
        }
    }

    public static PerfProtocolType getInstance(Integer type) {
        if (typeToValueMap.containsKey(type)) {
            return typeToValueMap.get(type);
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
