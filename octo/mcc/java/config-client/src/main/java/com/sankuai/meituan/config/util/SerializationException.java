package com.sankuai.meituan.config.util;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-23
 */
public class SerializationException extends RuntimeException {
    private static final long serialVersionUID = -8982982722792664941L;

    public SerializationException() {
        super();
    }

    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }

    public SerializationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
