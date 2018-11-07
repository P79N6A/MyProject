package com.sankuai.mtthrift.testSuite.annotationTest;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.meituan.service.mobile.mtthrift.annotation.AbstractThriftException;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-18
 * Time: 下午4:48
 */
@ThriftStruct
public final class TwitterUnavailable extends AbstractThriftException {

    private String message;

    public TwitterUnavailable() {
    }

    @ThriftConstructor
    public TwitterUnavailable(String message) {
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
