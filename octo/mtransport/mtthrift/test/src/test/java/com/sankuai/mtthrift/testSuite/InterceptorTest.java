package com.sankuai.mtthrift.testSuite;

import com.sankuai.mtthrift.testSuite.idlTest.Tweet;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import com.sankuai.mtthrift.testSuite.idlTest.TwitterUnavailable;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/4/17
 * Time: 11:26
 */
public class InterceptorTest {
    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/intercept/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/intercept/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        Thread.sleep(25000);
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    @Test
    public void test() throws TException {
        client.testString("hello");
        try {
            client.testException(new Tweet(1, "a", "b"));
        } catch (TwitterUnavailable twitterUnavailable) {

        }
    }
}
