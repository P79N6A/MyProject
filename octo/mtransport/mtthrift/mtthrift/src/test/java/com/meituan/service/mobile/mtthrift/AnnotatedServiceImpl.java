package com.meituan.service.mobile.mtthrift;

import org.apache.thrift.TException;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/8
 * Time: 14:34
 */
public class AnnotatedServiceImpl implements AnnotatedService {
    @Override
    public String echo(String content) throws TException {
        return content;
    }
}
