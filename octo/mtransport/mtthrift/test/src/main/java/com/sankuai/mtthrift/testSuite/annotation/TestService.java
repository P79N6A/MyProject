package com.sankuai.mtthrift.testSuite.annotation;

import com.facebook.swift.service.ThriftException;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import org.apache.thrift.TException;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-9-8
 * Time: 下午3:00
 */
@ThriftService
public interface TestService {

    @ThriftMethod
    public String testNull() throws TException;

    @ThriftMethod(exception = {@ThriftException(type = MyException.class, id = 1),
            @ThriftException(type = InternalErrorException.class, id = 2)})
    public String testException() throws MyException, InternalErrorException, TException;

    @ThriftMethod
    public String testMock(String str) throws TException;

    @ThriftMethod
    public void testTimeout() throws TException;

    @ThriftMethod
    public TestResponse testStruct(TestRequest testRequest) throws
            TException;

    @ThriftMethod(exception = {@ThriftException(type = MyException.class, id = 1)})
    public int testBaseTypeException() throws MyException, TException;
}
