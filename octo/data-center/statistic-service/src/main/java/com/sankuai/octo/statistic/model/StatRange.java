package com.sankuai.octo.statistic.model;

import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftEnumValue;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 天的有时间范围是86400 , 6*60 = 86700 有效期
 * 小时: ...
 * 分钟: ...
 */

@ThriftEnum
public enum StatRange {
    Day(86400, 86450),//延迟五十秒
    Hour(3600, 3650),//延迟五十秒
    Minute(60, 110);//延迟五十秒

    // 枚举对象的 时间范围 属性,单位 秒
    private int timeRange;

    //生命周期
    private int lifetime;

    private static final Map<String, StatRange> values = new HashMap<>();

    static {
        for (StatRange range : StatRange.values()) {
            values.put(range.toString().toLowerCase(), range);
        }
    }

    public static StatRange getInstance(String range) {
        if (StringUtils.hasText(range) && values.containsKey(range.toLowerCase())) {
            return values.get(range.toLowerCase());
        } else {
            return StatRange.Minute;
        }
    }

    // 枚举对象构造函数
    StatRange(int timeRange, int lifetime) {
        this.timeRange = timeRange;
        this.lifetime = lifetime;
    }

    // 枚举对象获取 时间范围的方法
    public int getTimeRange() {
        return this.timeRange;
    }

    //获取 在内存里的 生命周期
    public int getLifetime() {
        return lifetime;
    }

    @ThriftEnumValue
    public int getIntValue() {
        return this.ordinal();
    }
}
