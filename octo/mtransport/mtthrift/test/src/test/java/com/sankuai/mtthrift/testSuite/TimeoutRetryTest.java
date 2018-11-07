package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/8/8
 * Time: 11:23
 */
public class TimeoutRetryTest {
    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;
    private static Twitter.Iface nettyClient;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/timeoutRetry/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/timeoutRetry/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        Thread.sleep(30000);

        ContextStore.getRequestMap().clear();
        ContextStore.getResponseMap().clear();
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
        if (!ContextStore.getRequestMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getRequestMap().size());
        }
        if (!ContextStore.getResponseMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getResponseMap().size());
        }
    }

    @Test
    public void test() {
        long start = System.currentTimeMillis();
        try {
            client.testString("hello");
        } catch (TException e) {
            assert e.getMessage()!= null && e.getMessage().contains("");
        }
    }
}
