package com.sankuai.mtthrift.testSuite;

import com.meituan.service.mobile.mtthrift.callback.OctoObserver;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.sankuai.mtthrift.testSuite.annotation.InternalErrorException;
import com.sankuai.mtthrift.testSuite.annotation.MyException;
import com.sankuai.mtthrift.testSuite.annotation.TestRequest;
import com.sankuai.mtthrift.testSuite.annotation.TestService;
import com.sankuai.mtthrift.testSuite.annotationTest.Twitter;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (C) 2017 Meituan
 * All rights reserved
 * User: xiongjiyuan
 * Date: 2017/2/14
 * Time: 18:39
 */
public class AsyncAnnotationTest {
    private static ClassPathXmlApplicationContext serverBeanFactory;
    private static ClassPathXmlApplicationContext clientBeanFactory;
    private static Twitter client;
    private static TestService client1;
    private static CountDownLatch countDownLatch;

    @BeforeClass
    public static void init() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/asyncanno/asyncserver.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/asyncanno/async.xml");
        countDownLatch = clientBeanFactory.getBean("countdown", CountDownLatch.class);
        client = clientBeanFactory.getBean("clientProxy", Twitter.class);
        client1 = clientBeanFactory.getBean("clientProxy1", TestService.class);
        Thread.sleep(30000);
        ContextStore.getRequestMap().clear();
        ContextStore.getResponseMap().clear();
    }

    @AfterClass
    public static void destroy() {
        serverBeanFactory.destroy();
        clientBeanFactory.destroy();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        if (!ContextStore.getRequestMap().isEmpty()) {
            System.out.println(ContextStore.getRequestMap());
            Assert.fail("actual size: " + ContextStore.getRequestMap().size());
        }
        if (!ContextStore.getResponseMap().isEmpty()) {
            System.out.println(ContextStore.getResponseMap());
            Assert.fail("actual size: " + ContextStore.getResponseMap().size());
        }
    }

    @Test
    public void testFuture() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        try {
            long count = countDownLatch.getCount()/2;
            for (int i = 0; i < count; i++) {
                client.testString("meituan"+i);
                Future future = ContextStore.getFuture();
                assert (future.get().equals("meituan"+i));

                client.testI32(i);
                future = ContextStore.getFuture();
                assert(future.get().equals(i));
            }
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testFuture1() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        try {
            long count = countDownLatch.getCount()/2;
            for (int i = 0; i < count; i++) {
                OctoThriftCallback callback = new OctoThriftCallback();
                ContextStore.setCallBack(callback);
                client.testString("meituan"+i);
                Future future = callback.getFuture();
                assert (future.get().equals("meituan"+i));

                callback = new OctoThriftCallback();
                ContextStore.setCallBack(callback);
                client.testI32(i);
                future = callback.getFuture();
                assert(future.get().equals(i));
            }
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testObserver() {
        try {
            long count = countDownLatch.getCount()/2;
            for (int i = 0; i < count; i++) {
                OctoThriftCallback callback = new OctoThriftCallback();
                callback.addObserver(new MyOctoObserver(countDownLatch, "meituan"+i));
                ContextStore.setCallBack(callback);
                client.testString("meituan"+i);
//                ContextStore.removeCallback();
                callback = new OctoThriftCallback();
                callback.addObserver(new MyOctoObserver(countDownLatch, i));
                ContextStore.setCallBack(callback);
                client.testI32(i);
//                ContextStore.removeCallback();
            }
            countDownLatch.await();
        } catch (TException e) {
            Assert.fail(e.getMessage());
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testException() throws InternalErrorException, MyException, TException, InterruptedException {
        try {
            client1.testException();
            System.out.println(ContextStore.getFuture().get());
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause() instanceof MyException);
        }

    }

    @Test
    public void testBaseTypeException() throws ExecutionException, InterruptedException, TException, MyException {
        try {
            client1.testBaseTypeException();
            System.out.println(ContextStore.getFuture().get());
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause() instanceof MyException);
        }
    }

    @Test
    public void testMock() {
        try {
            client1.testMock("hello");
            System.out.println(ContextStore.getFuture().get());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testNull() {
        try {
            client1.testNull();
            System.out.println(ContextStore.getFuture().get());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testStruct() {
        try {
            client1.testStruct(new TestRequest());
            System.out.println(ContextStore.getFuture().get());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testTimeout() throws TException, ExecutionException, InterruptedException {
        try {
            client1.testTimeout();
            System.out.println(ContextStore.getFuture().get());
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("timeout"));
        }
    }
}

class MyOctoObserver implements OctoObserver {
    private CountDownLatch countDownLatch;
    private Object expectedResult;
    public MyOctoObserver(CountDownLatch countDownLatch, Object expectedResult) {
        this.countDownLatch = countDownLatch;
        this.expectedResult = expectedResult;
    }

    @Override
    public void onSuccess(Object result) {
        if (expectedResult.getClass().isPrimitive()) {
            Assert.assertEquals(expectedResult, result);
        } else {
            Assert.assertTrue(expectedResult.equals(result));
        }
        countDownLatch.countDown();
    }

    @Override
    public void onFailure(Throwable e) {
        Assert.fail(e.getMessage());
    }
}
