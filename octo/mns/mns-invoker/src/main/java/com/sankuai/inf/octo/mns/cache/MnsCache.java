package com.sankuai.inf.octo.mns.cache;

public class MnsCache<R, C, V> {
    private MultiMap<R, C, V> cache = MultiMap.create();
    private CacheLoader<R, C, V> loader;

    public MnsCache() {
    }

    public MnsCache(CacheLoader<R, C, V> loader) {
        this.loader = loader;
    }

    public static <R, C, V> MnsCache<R, C, V> create() {
        MnsCache<R, C, V> instance = new MnsCache<R, C, V>();
        return instance;
    }

    public static <R, C, V> MnsCache<R, C, V> create(
            CacheLoader<R, C, V> loader) {
        MnsCache<R, C, V> instance = new MnsCache<R, C, V>(loader);
        return instance;
    }

    public void put(R row, C column, V value) {
        cache.put(row, column, value);
    }

    public V get(R row, C column) {
        return cache.get(row, column);
    }

    public boolean contains(R row, C column) {
        return cache.contains(row, column);
    }

    public void updateAll() {
        if (loader != null) {
            for (R row : cache.rows()) {
                for (C column : cache.columns(row)) {
                    V value = loader.reload(row, column);
                    if (null != value) {
                        put(row, column, value);
                    }
                }
            }
        }
    }
}
