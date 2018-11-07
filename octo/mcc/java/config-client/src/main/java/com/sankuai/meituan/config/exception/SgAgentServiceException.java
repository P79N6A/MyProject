package com.sankuai.meituan.config.exception;

public class SgAgentServiceException extends IllegalStateException {
    private Integer errorCode;
    private String errorMsg;

    public SgAgentServiceException(String s) {
        super(s);
    }

    public SgAgentServiceException(String s, Throwable throwable) {
        super(s, throwable);
        if (throwable instanceof SgAgentServiceException) {
            SgAgentServiceException sgAgentServiceException = (SgAgentServiceException) throwable;
            this.errorCode = sgAgentServiceException.errorCode;
            this.errorMsg = sgAgentServiceException.errorMsg;
        }
    }

    public SgAgentServiceException(String msg, Integer errorCode, String errorMsg) {
        super(msg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
