package com.meituan.service.mobile.mtthrift.netty.exception;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/6
 * Time: 11:47
 */
public class ChannelPoolException extends RuntimeException {
    public ChannelPoolException(String message) {
        super(message);
    }
}
