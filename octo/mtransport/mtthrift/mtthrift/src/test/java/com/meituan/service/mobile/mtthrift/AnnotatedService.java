package com.meituan.service.mobile.mtthrift;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import org.apache.thrift.TException;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/8
 * Time: 14:28
 */
@ThriftService
public interface AnnotatedService {
    @ThriftMethod
    String echo(String content) throws TException;
}
