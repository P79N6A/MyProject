package com.sankuai.inf.octo.mns.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MultiMap<R, C, V> {
    private static final Logger LOG = LoggerFactory.getLogger(MultiMap.class);
    private Map<R, Map<C, V>> maps = new ConcurrentHashMap<R, Map<C, V>>();

    public static <R, C, V> MultiMap<R, C, V> create() {
        return new MultiMap<R, C, V>();
    }

    public Set<R> rows() {
        return maps.keySet();
    }

    public Set<C> columns(R row) {
        Map<C, V> subMap = get(row);
        if (subMap != null) {
            return subMap.keySet();
        } else {
            return Collections.emptySet();
        }
    }

    public void put(R row, C column, V value) {
        if (row != null && column != null && value != null) {
            Map<C, V> subMap = getOrCreate(row);
            subMap.put(column, value);
        } else {
            LOG.debug("row {} or column {} or value {} is null",
                    new Object[]{row, column, value});
        }
    }

    public V get(R row, C column) {
        Map<C, V> subMap = get(row);
        if (subMap != null && column != null) {
            return subMap.get(column);
        } else {
            return null;
        }
    }

    public Map<C, V> get(R row) {
        if (row == null) {
            return null;
        } else {
            return maps.get(row);
        }
    }

    public Map<C, V> getOrCreate(R row) {
        Map<C, V> subMap = get(row);
        if (null == subMap && row != null) {
            subMap = new ConcurrentHashMap<C, V>();
            maps.put(row, subMap);
        }
        return subMap;
    }

    public boolean contains(R row, C column) {
        return get(row, column) != null;
    }
}
