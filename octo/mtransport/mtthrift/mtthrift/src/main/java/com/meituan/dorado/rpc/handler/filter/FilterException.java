package com.meituan.dorado.rpc.handler.filter;

/**
 * 用于过滤器中抛出
 */
public class FilterException extends RuntimeException {

    public FilterException(String message) {
        super(message);
    }

    public FilterException(String s, Throwable e) {
        super(s, e);
    }
}
