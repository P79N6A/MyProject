package com.sankuai.meituan.config.exception;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-23
 */
public class MtConfigException extends RuntimeException {
    public MtConfigException() {
        super();
    }
    public MtConfigException(String msg) {
        super(msg);
    }
    public MtConfigException(Throwable cause) {
        super(cause);
    }
    public MtConfigException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
