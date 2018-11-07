package com.sankuai.meituan.config.errocode;

import com.sankuai.meituan.config.exception.ErrorCode;
import com.sankuai.octo.config.model.ConfigGroupResponse;
import org.junit.Test;

/**
 * Created by barneyhu on 15/11/19.
 */
public class ErrorCodeTest {
    @Test
    public void testSucceedCode() {
        System.out.println("errorCode = " + ErrorCode.SUCCESS.getErrCode());
        System.out.println("errorMsg = " + ErrorCode.SUCCESS.getErrMsg());
    }

    @Test
    public void testResponse() {
        ConfigGroupResponse resp = new ConfigGroupResponse();
        resp.setCode(ErrorCode.SUCCESS.getErrCode());
        resp.setErrMsg(ErrorCode.SUCCESS.getErrMsg());
        System.out.println("errorCode = " + resp.getCode());
        System.out.println("errorMsg  = " + resp.getErrMsg());
    }
}
