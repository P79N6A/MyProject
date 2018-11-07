package com.sankuai.octo.msgp.utils;

/**
 * Created by wangyanzhao on 15/4/20.
 */
public class CustomGenericException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String errCode;
    private String errMsg;

    //getter and setter methods

    public CustomGenericException(String errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public String getErrCode() {
        return this.errCode ;
    }

}