package com.sankuai.mtthrift.testSuite;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.meituan.service.mobile.mtthrift.callback.OctoObserver;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.meituan.service.mobile.mtthrift.netty.ContextStore;
import com.sankuai.mtthrift.testSuite.idlTest.Twitter;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class AsyncTest {

    static ClassPathXmlApplicationContext serverBeanFactory;
    static ClassPathXmlApplicationContext clientBeanFactory;
    static Twitter.AsyncIface thriftClient;
    static Twitter.AsyncIface nettyClient;
    static com.sankuai.mtthrift.testSuite.annotationTest.Twitter annotationNettyClient;
    static ExecutorService executor = Executors.newFixedThreadPool(5);

    @BeforeClass
    public static void start() throws InterruptedException {
        serverBeanFactory = new ClassPathXmlApplicationContext("testSuite/idl/asyncServer.xml");
        clientBeanFactory = new ClassPathXmlApplicationContext("testSuite/idl/asyncclient.xml");
        thriftClient = (Twitter.AsyncIface) clientBeanFactory.getBean("clientProxy");
        nettyClient = (Twitter.AsyncIface) clientBeanFactory.getBean("nettyClientProxy");
        annotationNettyClient = (com.sankuai.mtthrift.testSuite.annotationTest.Twitter) clientBeanFactory.getBean("annotationNettyClientProxy");
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
    public void testCallback() {

        OctoThriftCallback callback1 = new OctoThriftCallback(executor);
        callback1.addObserver(new OctoObserver() {
            public void onSuccess(Object result) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Assert.fail(e.getMessage());
                }
                System.out.println(result);
                assert ("callback1".equals(result));
            }

            public void onFailure(Throwable e) {
                System.out.println("onFailure:" + e.getMessage());
            }
        });

        OctoThriftCallback callback2 = new OctoThriftCallback(executor);
        callback2.addObserver(new OctoObserver() {
            public void onSuccess(Object result) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Assert.fail(e.getMessage());
                }
                System.out.println(result);
                assert ("callback2".equals(result));
            }

            public void onFailure(Throwable e) {
                System.out.println("onFailure:" + e.getMessage());
            }
        });

        try {
            thriftClient.testString("callback1", callback1);
            nettyClient.testString("callback2", callback2);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testFuture() {
        OctoThriftCallback callback1 = new OctoThriftCallback();
        OctoThriftCallback callback2 = new OctoThriftCallback();
        try {
            thriftClient.testString("future", callback1);
            nettyClient.testString("future", callback2);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
        Future future1 = callback1.getFuture();
        Future future2 = callback2.getFuture();
        long start = System.currentTimeMillis();
        try {
            Object result1 = future1.get();
            System.out.println(result1);
            assert ("future".equals(result1));

            Object result2 = future2.get();
            System.out.println(result2);
            assert ("future".equals(result2));
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        } catch (ExecutionException e) {
            Assert.fail(e.getMessage());
        }

        System.out.println(System.currentTimeMillis() - start + " ms~~~");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testListennableFuture() throws IOException, ExecutionException, InterruptedException {

        OctoThriftCallback<Twitter.AsyncClient.testString_call, Object> testStringCallback = new OctoThriftCallback();
        OctoThriftCallback<Twitter.AsyncClient.testString_call, Object> testStringCallback1 = new OctoThriftCallback();
        OctoThriftCallback<Twitter.AsyncClient.testI32_call, Object> testI32Callback = new OctoThriftCallback();
        OctoThriftCallback<Twitter.AsyncClient.testI32_call, Object> testI32Callback1 = new OctoThriftCallback();

        long start = System.currentTimeMillis();
        try {
            thriftClient.testString("meituan", testStringCallback);
            nettyClient.testString("dianping", testStringCallback1);
            thriftClient.testI32(3, testI32Callback);
            nettyClient.testI32(3, testI32Callback1);
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }

        SettableFuture testStringFuture = testStringCallback.getSettableFuture();
        SettableFuture testStringFuture1 = testStringCallback1.getSettableFuture();
        SettableFuture testI32Future = testI32Callback.getSettableFuture();
        SettableFuture testI32Future1 = testI32Callback1.getSettableFuture();

        ListenableFuture<List<Object>> listenableFuture = Futures.successfulAsList(testStringFuture, testStringFuture1, testI32Future, testI32Future1);

        List<Object> list = listenableFuture.get();
        for (Object obj : list) {
            System.out.println(obj);
        }

        System.out.println(System.currentTimeMillis() - start + "ms");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testAnnotationFuture() {
        try {
            annotationNettyClient.testString("annotation");
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
        SettableFuture future = ContextStore.getSettableFuture();
        try {
            String result = (String) future.get();
            System.out.println(result);
            assert ("annotation".equals(result));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testAnnotationCallback() {

        OctoThriftCallback helloCallback = new OctoThriftCallback();
        helloCallback.addObserver(new OctoObserver<String>() {
            public void onSuccess(String result) {
                System.out.println(result);
                assert ("annotation".equals(result));
            }

            public void onFailure(Throwable e) {
                e.printStackTrace();
            }
        });

        try {
            ContextStore.setCallBack(helloCallback);
            annotationNettyClient.testString("annotation");
        } catch (TException e) {
            Assert.fail(e.getMessage());
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }
}
