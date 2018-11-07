package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.sankuai.mtthrift.testSuite.annotation.*;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-9-25
 * Time: 上午11:31
 */
public class AnnotationTest {

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static TestService client;
    private static TestService nettyClient;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/annotation/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/annotation/client.xml");
        client = (TestService) clientBeanFactory.getBean("clientProxy");
        nettyClient = (TestService) clientBeanFactory.getBean("nettyClientProxy");
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
    public void testNull() {
        try {
            client.testNull();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testException() {
        try {
            client.testException();
            Assert.fail();
        } catch (MyException e) {
            //test passed
        } catch (InternalErrorException e) {
            Assert.fail(e.getMessage());
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testStruct() {
        try {
            TestRequest testRequest = new TestRequest();
            testRequest.setUserid(123);
            testRequest.setName("土豆");
            testRequest.setMessage("你是谁");
            testRequest.setSeqid(1);
            TestResponse testResponse = client.testStruct(testRequest);
            System.out.println(testResponse);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nettyTestNull() {
        try {
            nettyClient.testNull();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nettyTestException() {
        try {
            nettyClient.testException();
            Assert.fail();
        } catch (MyException e) {
            //test passed
        } catch (InternalErrorException e) {
            Assert.fail(e.getMessage());
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nettyTestStruct() {
        try {
            TestRequest testRequest = new TestRequest();
            testRequest.setUserid(123);
            testRequest.setName("土豆");
            testRequest.setMessage("你是谁");
            testRequest.setSeqid(1);
            TestResponse testResponse = nettyClient.testStruct(testRequest);
            System.out.println(testResponse);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }
}
