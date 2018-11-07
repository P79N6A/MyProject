package com.meituan.service.mobile.mtthrift;

import org.apache.thrift.TException;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-10-19
 * Time: 下午5:28
 */
public class EchoServiceImpl implements EchoService.Iface {
    @Override
    public String echo(String username) throws TException {
        System.out.println("echo:" + username);
        return username;
    }
}
