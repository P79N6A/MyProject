package com.sankuai.meituan.config.function;

public interface Consumer<T> {
    void accept(T t);
}
