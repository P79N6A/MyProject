package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.meituan.service.mobile.mtthrift.netty.exception.RequestTimeoutException;
import com.sankuai.mtthrift.testSuite.idl.MyException;
import com.sankuai.mtthrift.testSuite.idl.TestService;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/27
 * Time: 13:25
 */
public class AsyncExceptionTest {
    static ClassPathXmlApplicationContext serverBeanFactory;
    static ClassPathXmlApplicationContext clientBeanFactory;
    static TestService.AsyncIface client;
    static TestService.AsyncIface nettyClient;

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/async/exceptionServer.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/async/exceptionClient.xml");
        client = (TestService.AsyncIface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (TestService.AsyncIface) clientBeanFactory.getBean("nettyClientProxy");
        Thread.sleep(30000);
        ContextStore.getRequestMap().clear();
        ContextStore.getResponseMap().clear();
    }

    @AfterClass
    public static void end() {
        clientBeanFactory.close();
        serverBeanFactory.close();
        if (!ContextStore.getRequestMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getRequestMap().size());
        }
        if (!ContextStore.getResponseMap().isEmpty()) {
            Assert.fail("actual size: " + ContextStore.getResponseMap().size());
        }
    }

    @Test
    public void nettyTestMock() throws ExecutionException, InterruptedException {
        try {
            System.out.println("testMock");
            OctoThriftCallback callback = new OctoThriftCallback();
            client.testMock("mock", callback);
            assert (callback.getFuture().get().equals("mock"));
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testTimeout() throws InterruptedException {
        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            client.testTimeout(callback);
            callback.getFuture().get();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause() instanceof TimeoutException);
        }
    }

    @Test
    public void testException() throws InterruptedException {
        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            client.testException(callback);
            callback.getFuture().get();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause().getCause() instanceof MyException);
        }

        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            client.testBaseTypeException(callback);
            callback.getFuture().get();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause().getCause() instanceof MyException);
        }
    }

    @Test
    public void testNull() throws ExecutionException, InterruptedException {
        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            client.testNull(callback);
            callback.getFuture().get();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            assert (e.getCause().getCause() instanceof TApplicationException);
            assert (e.getCause().getMessage().contains("NullPointerException"));
        }
    }


    @Test
    public void testReturnNull() throws ExecutionException, InterruptedException {
        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            client.testReturnNull(callback);
            String result = (String) callback.getFuture().get();

            assert (null == result);
        } catch (TException e) {
            assert (e.getCause() instanceof TApplicationException);
            assert (e.getMessage().contains("NullPointerException"));// .getCause() instanceof NullPointerException);
        }
    }

    @Test
    public void testMock() throws ExecutionException, InterruptedException {
        try {
            System.out.println("testMock");
            OctoThriftCallback callback = new OctoThriftCallback();
            nettyClient.testMock("mock", callback);
            String result = (String) callback.getFuture().get();
            assert (result.equals("mock"));
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void nettyTestTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            nettyClient.testTimeout(callback);
            callback.getFuture().get(30000, TimeUnit.MILLISECONDS);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause() instanceof RequestTimeoutException);
        }
    }

    @Test
    public void nettyTestException() throws InterruptedException {
        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            nettyClient.testException(callback);
            callback.getFuture().get();
            Assert.fail();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause() instanceof MyException);
            Assert.assertEquals("error", e.getCause().getMessage());
        }
    }

    @Test
    public void nettyTestNull() throws ExecutionException, InterruptedException {
        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            nettyClient.testNull(callback);
            callback.getFuture().get();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            assert (e.getCause() instanceof TApplicationException);
            assert (e.getCause().getMessage().contains("NullPointerException"));
        }
    }


    @Test
    public void nettyTestReturnNull() throws ExecutionException, InterruptedException {
        try {
            OctoThriftCallback callback = new OctoThriftCallback();
            nettyClient.testReturnNull(callback);
            String result = (String) callback.getFuture().get();
            assert (null == result);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            assert (e.getCause() instanceof TApplicationException);
            assert (e.getMessage().contains("NullPointerException"));
        }
    }
}
