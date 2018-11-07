package com.sankuai.mtthrift.testSuite;

import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 17-2-13
 * Time: 上午11:37
 */
public class MultiServiceTest {

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface idlClient;
    private static com.sankuai.mtthrift.testSuite.annotationTest.Twitter annotationClient;
    private static Twitter.Iface idlClient2;
    private static com.sankuai.mtthrift.testSuite.annotationTest.Twitter annotationClient2;

    @BeforeClass
    public static void start() throws InterruptedException {
        Thread.sleep(10000);
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/multiService/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/multiService/client.xml");
        idlClient = (Twitter.Iface) clientBeanFactory.getBean("idlClientProxy");
        annotationClient = (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("annotationClientProxy");
        idlClient2 = (Twitter.Iface) clientBeanFactory.getBean("idlClientProxy2");
        annotationClient2 = (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("annotationClientProxy2");
        Thread.sleep(30000);
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    @Test
    public void baseTest() {
        String result = "";

        try {
            result = idlClient.testString("hello");
        } catch (TException e) {
            e.printStackTrace();
        }
        assert ("hello".equals(result));
        System.out.println(result);

        try {
            result = annotationClient.testString("world");
        } catch (TException e) {
            e.printStackTrace();
        }
        assert ("world".equals(result));
        System.out.println(result);
    }

    @Test
    public void serviceIsolationTest() {
        String result = "";

        try {
            result = idlClient2.testString("hello");
        } catch (TException e) {
            e.printStackTrace();
        }
        assert ("hello".equals(result));
        System.out.println(result);

        try {
            result = annotationClient2.testString("world");
        } catch (TException e) {
            e.printStackTrace();
        }
        assert ("world".equals(result));
        System.out.println(result);
    }
}