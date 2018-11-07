package com.meituan.service.mobile.mtthrift.auth;

import org.apache.thrift.TException;

/**
 * Author: caojiguang@gmail.com
 * Date: 15/10/28
 * Description:
 */
public class AuthFailedException extends TException {
    public AuthFailedException() {
        this("authorized exception");
    }

    public AuthFailedException(String msg) {
        super(msg);
    }
}
