package com.sankuai.mtthrift.testSuite;

import com.google.common.util.concurrent.SettableFuture;
import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.ExecutionException;


/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-12-28
 * Time: 下午2:20
 */
public class AuthTest {

    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static ClassPathXmlApplicationContext serverBeanFactory;

    private static Twitter.Iface syncClient;
    private static Twitter.AsyncIface asyncClient;
    private static Twitter.Iface unAuthedSyncClient;
    private static Twitter.AsyncIface unAuthedAsyncClient;
    private static Twitter.Iface userDefinedSyncClient;
    private static Twitter.AsyncIface userDefinedAsyncClient;

    private static com.sankuai.mtthrift.testSuite.annotationTest.Twitter annotationClient;
    private static com.sankuai.mtthrift.testSuite.annotationTest.Twitter unAuthedAnnotationClient;
    private static com.sankuai.mtthrift.testSuite.annotationTest.Twitter userDefinedAnnotationSyncClient;

    private static Twitter.AsyncIface asyncNettyIOClient;
    private static com.sankuai.mtthrift.testSuite.annotationTest.Twitter asyncAnnotationNettyIOClient;

    private static Twitter.Iface kmsAuthClient;
    private static Twitter.Iface kmsDirectAuthClient;
    private static Twitter.AsyncIface kmsAsyncAuthClient;

    private static Twitter.Iface myAuthClient;

    @BeforeClass
    public static void start() throws InterruptedException {

        ThriftServerGlobalConfig config = new ThriftServerGlobalConfig();
        config.setEnableAuthDebugLog(true);

        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/auth/server.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/auth/client.xml");
        syncClient = (Twitter.Iface) clientBeanFactory.getBean("syncClientProxy");
        asyncClient = (Twitter.AsyncIface) clientBeanFactory.getBean("asyncClientProxy");
        unAuthedSyncClient = (Twitter.Iface) clientBeanFactory.getBean("unAuthedSyncClientProxy");
        unAuthedAsyncClient = (Twitter.AsyncIface) clientBeanFactory.getBean("unAuthedAsyncClientProxy");
        userDefinedSyncClient = (Twitter.Iface) clientBeanFactory.getBean("userDefinedSyncClientProxy");
        userDefinedAsyncClient = (Twitter.AsyncIface) clientBeanFactory.getBean("userDefinedAsyncClientProxy");
        annotationClient = (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("annotationClientProxy");
        unAuthedAnnotationClient = (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("unAuthedAnnotationClientProxy");
        userDefinedAnnotationSyncClient = (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("userDefinedAnnotationSyncClientProxy");

        asyncNettyIOClient = (Twitter.AsyncIface) clientBeanFactory.getBean("asyncNettyIOClientProxy");
        asyncAnnotationNettyIOClient = (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("asyncAnnotationNettyIOClientProxy");

        kmsAuthClient = (Twitter.Iface) clientBeanFactory.getBean("kmsAuthClientProxy");
        kmsDirectAuthClient = (Twitter.Iface) clientBeanFactory.getBean("kmsDirectAuthClientProxy");
        kmsAsyncAuthClient = (Twitter.AsyncIface) clientBeanFactory.getBean("kmsAsyncAuthClientProxy");
        myAuthClient = (Twitter.Iface) clientBeanFactory.getBean("myAuthDataSourceClientProxy");

        Thread.sleep(30000);
    }

    @AfterClass
    public static void stop() {
        clientBeanFactory.destroy();
        serverBeanFactory.destroy();
    }

    @Test
    public void testIdl() {
        Tracer.serverRecv(new TraceParam("test"));
        String result = "";
        try {
            result = syncClient.testString("hello");
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));

        try {
            unAuthedSyncClient.testString("hello");
        } catch (TException e) {
            System.out.println(e.getMessage());
            assert (e.getMessage().contains("AuthFailedException"));
        }

        try {
            result = userDefinedSyncClient.testString("hello");
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));

        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            asyncClient.testString("hello", callback);
            result = (String) callback.getFuture().get();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));

        try {
            unAuthedAsyncClient.testString("hello", new OctoThriftCallback());
        } catch (TException e) {
            System.out.println(e.getMessage());
            assert (e.getMessage().contains("AuthFailedException"));
        }

        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            userDefinedAsyncClient.testString("hello", callback);
            result = (String) callback.getFuture().get();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));
        Tracer.serverSend();
    }

    @Test
    public void testAnnotation() {

        Tracer.serverRecv(new TraceParam("test"));
        String result = "";
        try {
            result = annotationClient.testString("hello");
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));

        try {
            unAuthedAnnotationClient.testString("hello");
            Assert.fail();
        } catch (TException e) {
            assert (e.getMessage().contains("AuthFailedException"));
        }

        try {
            result = userDefinedAnnotationSyncClient.testString("hello");
        } catch (TException e) {
            Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
            assert (e.getMessage().contains("AuthFailedException"));
        }
        System.out.println(result);
        assert ("hello".equals(result));
        Tracer.serverSend();
    }

    @Test
    public void testKmsAuthDataSource() {

        String result = "";
        try {
            result = kmsAuthClient.testString("hello");
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));
    }

    @Test
    public void testKmsDirectAuthDataSource() {

        String result = "";
        try {
            result = kmsDirectAuthClient.testString("hello");
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));
    }

    @Test
    public void testKmsAsyncAuthDataSource() {

        String result = "";
        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            kmsAsyncAuthClient.testString("hello", callback);
            result = ((String) callback.getFuture().get());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));
    }

    @Test
    public void testAsyncIDL() {
        String result = "";
        OctoThriftCallback callback = new OctoThriftCallback();
        try {
            asyncNettyIOClient.testString("hello", callback);
            result = (String) callback.getFuture().get();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));
    }

    @Test
    public void testAsyncAnnotation() {
        String result = "";
        try {
            asyncAnnotationNettyIOClient.testString("hello");
            SettableFuture future = ContextStore.getSettableFuture();
            result = (String) future.get();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));
    }

    @Test
    public void testMyAuthDataSource() {
        String result = "";
        try {
            result = myAuthClient.testString("hello");
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
        System.out.println(result);
        assert ("hello".equals(result));
    }

    //    public static void main(String[] args) throws InterruptedException {
//        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/auth/server.xml");
//
//    }
}
