package com.sankuai.mtthrift.testSuite;


import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.atomic.AtomicInteger;


public class RejectedExceptionTest {

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static Twitter.Iface client;
    private static Twitter.AsyncIface asyncClient;
    private static Twitter.Iface nettyClient;
    private static Twitter.AsyncIface asyncNettyClient;

    private static AtomicInteger counter = new AtomicInteger(0);

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/rejectedException/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/rejectedException/client.xml");
        client = (Twitter.Iface) clientBeanFactory.getBean("clientProxy");
        asyncClient = (Twitter.AsyncIface) clientBeanFactory.getBean("asyncClientProxy");
        nettyClient = (Twitter.Iface) clientBeanFactory.getBean("nettyClientProxy");
        asyncNettyClient = (Twitter.AsyncIface) clientBeanFactory.getBean("asyncNettyClientProxy");
        Thread.sleep(25000);
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    @Test
    public void rejectTest() throws InterruptedException {

        Thread threadOne = new Thread(new Runnable() {
            public void run() {
                call();
            }
        });
        threadOne.start();

        Thread threadTwo = new Thread(new Runnable() {
            public void run() {
                call();
            }

        });
        threadTwo.start();

        threadOne.join();
        threadTwo.join();
    }

    public void call() {

        while (true) {

            if (counter.get() > 20) {
                break;
            }

            //sync thrift client
            try {
                String result = client.testString("sync thrift");
                System.out.println(result);
            } catch (Exception e) {
                if (e.getCause() != null && e.getCause().getMessage().contains("RejectedExecutionException")) {

                } else {
                    Assert.fail();
                }
            }

            //async thrift client
            try {
                OctoThriftCallback callback = new OctoThriftCallback();
                asyncClient.testString("async thrift", callback);
                System.out.println(callback.getFuture().get());
            } catch (Exception e) {
                if (e.getCause() != null && e.getCause().getMessage().contains("RejectedExecutionException")) {

                } else {
                    Assert.fail();
                }
            }

            //sync netty client
            try {
                String result = nettyClient.testString("sync netty");
                System.out.println(result);
            } catch (Exception e) {
                if (e.getCause() != null && e.getCause().getMessage().contains("RejectedExecutionException")) {

                } else {
                    Assert.fail();
                }
            }

            //async netty client
            try {
                OctoThriftCallback callback = new OctoThriftCallback();
                asyncNettyClient.testString("async netty", callback);
                System.out.println(callback.getFuture().get());
            } catch (Exception e) {
                if (e.getCause() != null && e.getCause().getMessage().contains("RejectedExecutionException")) {

                } else {
                    Assert.fail();
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            counter.incrementAndGet();
        }
    }

}
