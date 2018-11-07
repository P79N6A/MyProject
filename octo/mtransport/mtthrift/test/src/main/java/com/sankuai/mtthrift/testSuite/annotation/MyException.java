package com.sankuai.mtthrift.testSuite.annotation;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.meituan.service.mobile.mtthrift.annotation.AbstractThriftException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.protocol.TProtocol;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-9-8
 * Time: 下午2:56
 */
@ThriftStruct
public final class MyException extends AbstractThriftException {

    private String message;

    public MyException() {
    }

    @ThriftConstructor
    public MyException(String message) {
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