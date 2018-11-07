package com.sankuai.octo.statistic.model;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wujinwu on 15/12/1.
 */
public enum PerfRole {
    SERVER,
    CLIENT;

    private static final Map<String, PerfRole> valueMap = new HashMap<>();

    static {
        for (PerfRole role : PerfRole.values()) {
            valueMap.put(role.toString().toLowerCase(), role);
        }
    }

    public static PerfRole getInstance(String key) {
        if (StringUtils.hasText(key) && valueMap.containsKey(key.toLowerCase())) {
            return valueMap.get(key.toLowerCase());
        } else {
            return SERVER;
        }
    }
}
