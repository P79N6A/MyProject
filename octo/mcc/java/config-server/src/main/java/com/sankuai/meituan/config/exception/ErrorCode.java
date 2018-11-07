package com.sankuai.meituan.config.exception;

import com.sankuai.octo.config.model.Constants;

/**
 * Created by barneyhu on 15/11/12.
 * ERRORCODE:
 * -05xxooo;
 * -0501xxx: 文件配置
 */

public enum ErrorCode implements MccErr{
    SUCCESS(Constants.SUCCESS, "success"),
    ERROR_UNKOWN(Constants.UNKNOW_ERROR, "unknow error"),
    ERROR_DEFAULTGROUP_EXIST(-501001, "default group already exists"),
    ERROR_PARAM(-501002, "param error"),
    ERROR_IPINANOTHERGROUP(-501003, "ip is in another group"),
    ERROR_GROUP_EXIST(-501004, "group already exists"),
    ERROR_PARAM_APPKEY(-501005, "param appkey is missing"),
    ERROR_PARAM_ENV(-501006, "param env is missing"),
    ERROR_PARAM_GROUPNAME(-501007, "param groupname is missing"),
    ERROR_PARAM_GROUPID(-501008, "param groupid is missing"),
    ERROR_PARAM_IP(-501009, "param ip is missing");

    private int errCode;
    private String errMsg;

     ErrorCode(int i, String msg) {
         errCode = i;
         errMsg = msg;
    }

    public int getErrCode() {
        return this.errCode;
    }

    public String getErrMsg() {
        return this.errMsg;
    }
}
