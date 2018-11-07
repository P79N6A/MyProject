package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.callback.OctoObserver;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
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
 * Date: 2017/5/8
 * Time: 上午11:12
 */
public class ServerRequiredFieldTest {


    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;
    private static Twitter.AsyncIface asyncClient;
    private static Twitter.Iface nettyClient;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/serverRequired/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/serverRequired/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        asyncClient = (Twitter.AsyncIface) clientBeanFactory.getBean("asyncClientProxy");
        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");
        Thread.sleep(30000);
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    @Test
    public void testThrift() {

        try {
            client.testStruct("test");
        } catch (TException e) {
            System.out.println(e.getMessage());
            assert (e.getMessage().contains("Required field 'userName' was not present"));
        }
    }

    @Test
    public void testAsyncThrift() {

        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            callback.addObserver(new OctoObserver() {
                public void onSuccess(Object result) {

                }

                public void onFailure(Throwable e) {
                    System.out.println(e.getMessage());
                    assert (e.getMessage().contains("Required field 'userName' was not present"));
                }
            });
            asyncClient.testStruct("test", callback);
        } catch (TException e) {
            System.out.println(e.getMessage());
            assert (e.getMessage().contains("Required field 'userName' was not present"));
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNetty() {

        try {
            nettyClient.testStruct("test");
        } catch (TException e) {
            System.out.println(e.getMessage());
            assert (e.getMessage().contains("Required field 'userName' was not present"));
        }
    }

}
