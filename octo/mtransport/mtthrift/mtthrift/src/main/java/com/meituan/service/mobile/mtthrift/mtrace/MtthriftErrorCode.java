package com.meituan.service.mobile.mtthrift.mtrace;

/**
 * User: YangXuehua
 * Date: 14-3-13
 * Time: 上午10:33
 */
public class MtthriftErrorCode {
    private int code;
    private String message;

    public MtthriftErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "{" + "code=" + code + ", message='" + message + '\'' + '}';
    }
}
