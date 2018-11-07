
package com.meituan.mtrace.thrift;

/**
 * Generic exception class for Thrift.
 *
 */
@Deprecated
/**
 * mtthrift 1.6.0 - 1.6.2 引入, 待以后去除
 */
public class TException extends Exception {

    private static final long serialVersionUID = 1L;

    public TException() {
        super();
    }

    public TException(String message) {
        super(message);
    }

    public TException(Throwable cause) {
        super(cause);
    }

    public TException(String message, Throwable cause) {
        super(message, cause);
    }
}
