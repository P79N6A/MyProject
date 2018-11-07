package com.sankuai.octo.statistic.model;

import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftEnumValue;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@ThriftEnum
public enum StatEnv {
    Prod,
    Stage,
    Test;

    private static final Map<String, StatEnv> values = new HashMap<>();

    static {
        for (StatEnv env : StatEnv.values()) {
            values.put(env.toString().toLowerCase(), env);
        }
    }

    public static boolean isValid(String env) {
        return StringUtils.hasText(env) && values.containsKey(env.toLowerCase());
    }

    public static StatEnv getInstance(String env) {
        if (StringUtils.hasText(env) && values.containsKey(env.toLowerCase())) {
            return values.get(env.toLowerCase());
        } else {
            return StatEnv.Prod;
        }
    }

    @ThriftEnumValue
    public int getIntValue() {
        return this.ordinal();
    }
}
