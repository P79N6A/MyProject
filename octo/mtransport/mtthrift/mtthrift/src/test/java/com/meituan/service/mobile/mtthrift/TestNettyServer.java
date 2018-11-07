package com.meituan.service.mobile.mtthrift;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-8
 * Time: 下午3:22
 */
public class TestNettyServer {

    public static void main(String[] args)
    {
        BeanFactory beanFactory = new ClassPathXmlApplicationContext("helloNetty.xml");
    }
}
