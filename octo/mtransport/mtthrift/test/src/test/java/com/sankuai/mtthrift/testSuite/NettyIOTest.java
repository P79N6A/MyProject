package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.callback.OctoObserver;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/1/16
 * Time: 15:37
 */
public class NettyIOTest {

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;
    private static Twitter.AsyncIface asyncClient;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/netty/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/netty/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        asyncClient = (Twitter.AsyncIface) clientBeanFactory.getBean("asyncClientProxy");
        Thread.sleep(30000);
        ContextStore.getRequestMap().clear();
        ContextStore.getResponseMap().clear();
    }

    @AfterClass
    public static void stop() {
        serverBeanFactory.destroy();
        clientBeanFactory.destroy();
        if (!ContextStore.getRequestMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getRequestMap().size());
        }
        if (!ContextStore.getResponseMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getResponseMap().size());
        }
    }

    @Test
    public void test() throws IOException, InterruptedException {
        try {
            Assert.assertEquals("hello", client.testString("hello"));

            OctoThriftCallback callback = new OctoThriftCallback();
            asyncClient.testString("world", callback);
            Assert.assertEquals("world", callback.getFuture().get());

            final CountDownLatch countDownLatch = new CountDownLatch(1);
            OctoThriftCallback callback1 = new OctoThriftCallback();
            callback1.addObserver(new OctoObserver() {
                @Override
                public void onSuccess(Object result) {
                    Assert.assertEquals("meituan", result);
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(Throwable e) {
                    Assert.fail(e.getMessage());
                    countDownLatch.countDown();
                }
            });

            asyncClient.testString("meituan", callback1);
            countDownLatch.await();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
