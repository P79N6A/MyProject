package com.meituan.service.mobile.mtthrift.netty.exception;

import org.apache.thrift.TApplicationException;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/1/18
 * Time: 18:47
 */
public class RequestTimeoutException extends TApplicationException {
    private static final long serialVersionUID = 7807116866187108813L;

    public RequestTimeoutException() {
        super("request timeout");
    }
}
