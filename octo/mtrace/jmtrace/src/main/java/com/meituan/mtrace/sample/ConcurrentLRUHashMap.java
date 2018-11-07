package com.meituan.mtrace.sample;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: YangXuehua
 * Date: 14-4-14
 * Time: 上午10:27
 */
public class ConcurrentLRUHashMap<K, V> extends LinkedHashMap<K, V> {
    private int maxCapacity;

    public ConcurrentLRUHashMap(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public synchronized V put(K key, V value) {
        return super.put(key,value);
    }

    @Override
    public synchronized V remove(Object key) {
        return super.remove(key);
    }


    @Override
    protected boolean removeEldestEntry (Map.Entry<K, V> eldest) {
        return size () > this.maxCapacity;
    }
}
