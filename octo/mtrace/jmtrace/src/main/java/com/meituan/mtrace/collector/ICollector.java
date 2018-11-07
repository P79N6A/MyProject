package com.meituan.mtrace.collector;

public interface ICollector<T> {
    void collect(final T t);
}
