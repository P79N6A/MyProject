package com.sankuai.mtthrift.testSuite;

import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Copyright (C) 2016 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 16/12/5
 * Time: 上午11:02
 */
public class StressMarkTest {
    static ClassPathXmlApplicationContext clientContext;
    static ClassPathXmlApplicationContext serverContext;
    private static Twitter.Iface client;
    private static Twitter.Iface nettyClient;
    private static Twitter.AsyncIface asyncClient;
    private static Twitter.AsyncIface nettyAsyncClient;

    @BeforeClass
    public static void init() throws InterruptedException {
        serverContext = new ClassPathXmlApplicationContext("testSuite/stressMarkTest/server.xml");
        Thread.sleep(20000L);
        clientContext = new ClassPathXmlApplicationContext("testSuite/stressMarkTest/client.xml");
        client = clientContext.getBean("clientProxy", Twitter.Iface.class);
        nettyClient = clientContext.getBean("nettyClientProxy", Twitter.Iface.class);
        asyncClient = clientContext.getBean("asyncClientProxy", Twitter.AsyncIface.class);
        nettyAsyncClient = clientContext.getBean("nettyAsyncClientProxy", Twitter.AsyncIface.class);
    }

    @AfterClass
    public static void destroy() {
        clientContext.destroy();
        serverContext.destroy();
    }


    @Test
    public void testOneStep() {
        //模拟服务调用过程
        Tracer.serverRecv(new TraceParam("test service"));

        Tracer.putContext("foreverContext", "world");
        Tracer.putOneStepContext("oneStepContext", "hello");

        try {
            double result = client.testDouble(Math.PI);
            assert (Double.compare(result, Math.PI) == 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();

        //模拟服务调用过程
        Tracer.serverRecv(new TraceParam("test service"));

        Tracer.putContext("foreverContext", "world");
        Tracer.putOneStepContext("oneStepContext", "hello");

        try {
            double result = nettyClient.testDouble(Math.PI);
            assert (Double.compare(result, Math.PI) == 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();
    }

    @Test
    public void testTwoSteps() {
        String helloWorld = "hello world";

        //模拟服务调用过程
        Tracer.serverRecv(new TraceParam("test service"));

        Tracer.putContext("foreverContext", "world");
        Tracer.putOneStepContext("oneStepContext", "hello");
        try {
            String result = client.testString(helloWorld);
            Assert.assertEquals(helloWorld, result);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();

        //模拟服务调用过程
        Tracer.serverRecv(new TraceParam("test service"));

        Tracer.putContext("foreverContext", "world");
        Tracer.putOneStepContext("oneStepContext", "hello");
        try {
            String result = nettyClient.testString(helloWorld);
            Assert.assertEquals(helloWorld, result);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();
    }

    @Test
    public void testAsyncOneStep() {

        Tracer.serverRecv(new TraceParam("test service"));
        Tracer.putContext("foreverContext", "foreverContext");
        Tracer.putOneStepContext("oneStepContext", "oneStepContext");

        try {
            OctoThriftCallback<Twitter.AsyncClient.testI32_call, Integer> callback = new OctoThriftCallback();
            asyncClient.testI32(32, callback);
            int result = callback.getFuture().get();
            Assert.assertEquals(32, result);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();

        Tracer.serverRecv(new TraceParam("test service"));
        Tracer.putContext("foreverContext", "foreverContext");
        Tracer.putOneStepContext("oneStepContext", "oneStepContext");

        try {
            OctoThriftCallback<Twitter.AsyncClient.testI32_call, Integer> callback = new OctoThriftCallback();
            nettyAsyncClient.testI32(32, callback);
            int result = callback.getFuture().get();
            Assert.assertEquals(32, result);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();
    }

    @Test
    public void testAsyncTwoStep() {

        Tracer.serverRecv(new TraceParam("test service"));
        Tracer.putContext("foreverContext", "foreverContext");
        Tracer.putOneStepContext("oneStepContext", "oneStepContext");

        try {
            OctoThriftCallback<Twitter.AsyncClient.testI64_call, Long> callback = new OctoThriftCallback();
            asyncClient.testI64(32, callback);
            long result = callback.getFuture().get();
            Assert.assertEquals(32, result);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();

        Tracer.serverRecv(new TraceParam("test service"));
        Tracer.putContext("foreverContext", "foreverContext");
        Tracer.putOneStepContext("oneStepContext", "oneStepContext");

        try {
            OctoThriftCallback<Twitter.AsyncClient.testI64_call, Long> callback = new OctoThriftCallback();
            nettyAsyncClient.testI64(32, callback);
            long result = callback.getFuture().get();
            Assert.assertEquals(32, result);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();
    }

    @Test
    public void testTracerTest() {
        Tracer.serverRecv(new TraceParam("server test"));
        Tracer.setTest(true);
        try{
            client.testBool(true);
            Assert.assertTrue(Tracer.isTest());
        } catch (Exception e) {
           Assert.fail(e.getMessage());
        }
        Tracer.serverSend();

        Tracer.serverRecv(new TraceParam("server test"));
        try{
            byte b = 1;
            client.testByte(b);
            Assert.assertFalse(Tracer.isTest());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();

        Tracer.serverRecv(new TraceParam("server test"));
        Tracer.setTest(true);
        try{
            nettyClient.testBool(true);
            Assert.assertTrue(Tracer.isTest());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();

        Tracer.serverRecv(new TraceParam("server test"));
        try{
            byte b = 1;
            nettyClient.testByte(b);
            Assert.assertFalse(Tracer.isTest());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Tracer.serverSend();
    }

}
