package com.meituan.service.mobile.mtthrift.netty.exception;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/7
 * Time: 10:26
 */
public class NetworkException extends RuntimeException {
    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String s, Throwable e) {
        super(s, e);
    }
}
