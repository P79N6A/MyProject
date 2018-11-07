package com.sankuai.octo.statistic.model;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wujinwu on 15/12/1.
 */
public enum PerfDataType {
    ALL(0),
    SLOW(1),
    DROP(2),
    FAILURE(3);

    private static final Map<String, PerfDataType> valueMap = new HashMap<>();

    private static final Map<Integer, PerfDataType> typeToValueMap = new HashMap<>();

    static {
        for (PerfDataType dataType : PerfDataType.values()) {
            valueMap.put(dataType.toString().toLowerCase(), dataType);
            typeToValueMap.put(dataType.getType(), dataType);
        }
    }

    private int type;

    PerfDataType(int type) {
        this.type = type;
    }

    public static PerfDataType getInstance(String key) {
        if (StringUtils.hasText(key) && valueMap.containsKey(key.toLowerCase())) {
            return valueMap.get(key.toLowerCase());
        } else {
            return ALL;
        }
    }

    public static PerfDataType getInstance(Integer type) {
        if (typeToValueMap.containsKey(type)) {
            return typeToValueMap.get(type);
        } else {
            throw new IllegalArgumentException("wrong type");
        }
    }

    public int getType() {
        return type;
    }
}
