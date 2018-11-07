package com.meituan.service.mobile.mtthrift;

import org.apache.thrift.TException;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-8
 * Time: 下午3:21
 */
public class HelloServiceImpl implements HelloService.Iface {

    @Override
    public String sayHello(String username) throws TException {
        System.out.println(username);
        return "hello, " + username;
    }

    @Override
    public String sayBye(String username) throws TException {
        System.out.println(username);
        return "bye, " + username;
    }
}
