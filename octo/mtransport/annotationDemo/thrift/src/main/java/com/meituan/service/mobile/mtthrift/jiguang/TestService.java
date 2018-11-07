package com.meituan.service.mobile.mtthrift.jiguang;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import org.apache.thrift.TException;

/**
 * Created by jiguang on 15/7/23.
 */

@ThriftService
public interface TestService {

    @ThriftMethod
    public TestResponse method1(TestRequest testRequest) throws
            TException;
    @ThriftMethod
    public long method2(int i) throws
            TException;
    @ThriftMethod
    public String method3() throws
            TException;
    @ThriftMethod
    public String methodNPE();
}
