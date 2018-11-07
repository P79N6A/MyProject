package com.sankuai.mtthrift.testSuite.annotation;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.meituan.service.mobile.mtthrift.annotation.AbstractThriftException;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-9-8
 * Time: 下午2:56
 */
@ThriftStruct
public final class InternalErrorException extends AbstractThriftException {

    private String message;

    public InternalErrorException() {
    }

    @ThriftConstructor
    public InternalErrorException(String message) {
        this.message = message;
    }

    @ThriftField(1)
    public String getMessage() {
        return message;
    }

    @ThriftField
    public void setMessage(String message) {
        this.message = message;
    }

}