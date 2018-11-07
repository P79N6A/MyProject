package com.sankuai.inf.octo.mns.cache;

public interface CacheLoader<R, C, V> {
    V reload(R row, C column);
}
