package com.sankuai.mtthrift.testSuite.threadpool;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig;
import com.meituan.service.mobile.mtthrift.proxy.ThriftClientProxy;
import com.meituan.service.mobile.mtthrift.proxy.ThriftServerPublisher;
import org.apache.thrift.TException;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-18
 * Time: 下午4:03
 */
public class ThreadPoolTest {

    private static final int threadNum = 1000;
    private static final long fastMsgCount = 20000;
    private static final long slowMsgCount = 1000000;
    private static AtomicLong fastTotalCount = new AtomicLong(0);
    private static AtomicLong slowTotalCount = new AtomicLong(0);
    private static AtomicLong fastErrorCount = new AtomicLong(0);
    private static AtomicLong slowErrorCount = new AtomicLong(0);
    private static Histogram fastHistogram = new Histogram(new UniformReservoir(10000));
    private static Histogram slowHistogram = new Histogram(new UniformReservoir(10000));
    private static EchoService.Iface client;

    private static long fastEndTime;
    private static long slowEndTime;


    public static void main(String[] args) throws Exception {
        EchoServiceImpl impl = new EchoServiceImpl();
        ThriftServerPublisher publisher = new ThriftServerPublisher();
        publisher.setServiceInterface(Class.forName("com.sankuai.mtthrift.testSuite.threadpool.EchoService"));
        publisher.setServiceImpl(impl);
        publisher.setAppKey("com.sankuai.inf.mtthrift.testServer");
        publisher.setPort(10003);
        publisher.publish();


        ThriftClientProxy proxy = new ThriftClientProxy();
        MTThriftPoolConfig config = new MTThriftPoolConfig();
        config.setMaxActive(600);
        config.setMaxIdle(600);
        config.setMaxWait(600);
        proxy.setServiceInterface(Class.forName("com.sankuai.mtthrift.testSuite.threadpool.EchoService"));
        proxy.setLocalServerPort(10003);
        proxy.setMtThriftPoolConfig(config);
        proxy.afterPropertiesSet();
        client = (EchoService.Iface) proxy.getObject();


        Thread.sleep(5000);
        Long start = System.currentTimeMillis();
        Thread[] threads = new Thread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            threads[i] = new TestThread(i);
            threads[i].start();
        }
        for (int i = 0; i < threadNum; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("---------------------------------");
        System.out.println("TPS:" + fastHistogram.getCount() * 1000 / (fastEndTime - start));
        System.out.println("99th:" + fastHistogram.getSnapshot().get99thPercentile());
        System.out.println("95th:" + fastHistogram.getSnapshot().get95thPercentile());
        System.out.println("Mean:" + fastHistogram.getSnapshot().getMean());
        System.out.println("Median:" + fastHistogram.getSnapshot().getMedian());
        System.out.println("Max:" + fastHistogram.getSnapshot().getMax());
        System.out.println("Check histogram count:" + fastHistogram.getCount());
        System.out.println("Error count:" + fastErrorCount.get());
        System.out.println("Error rate:" + ((float) fastErrorCount.get()) / (fastErrorCount.get() + fastHistogram.getCount()) * 100 + "%");
        System.out.println("Thread count:" + threadNum / 2);
        System.out.println();
        System.out.println("TPS:" + slowHistogram.getCount() * 1000 / (slowEndTime - start));
        System.out.println("99th:" + slowHistogram.getSnapshot().get99thPercentile());
        System.out.println("95th:" + slowHistogram.getSnapshot().get95thPercentile());
        System.out.println("Mean:" + slowHistogram.getSnapshot().getMean());
        System.out.println("Median:" + slowHistogram.getSnapshot().getMedian());
        System.out.println("Max:" + slowHistogram.getSnapshot().getMax());
        System.out.println("check histogram count:" + slowHistogram.getCount());
        System.out.println("Error count:" + slowErrorCount.get());
        System.out.println("Error rate:" + ((float) slowErrorCount.get()) / (slowErrorCount.get() + fastHistogram.getCount()) * 100 + "%");
        System.out.println("thread count:" + threadNum / 2);


        proxy.destroy();
        publisher.destroy();
        System.exit(0);
    }

    static class TestThread extends Thread {

        long id = 0;
        private int index;

        public TestThread(int threadIndex) {
            index = threadIndex;
        }

        public void run() {

            while (true) {

                if (index % 2 == 0) {
                    id = fastTotalCount.incrementAndGet();
                    if (id > fastMsgCount) {
                        fastEndTime = System.currentTimeMillis();
                        slowTotalCount.set(slowMsgCount + 1);
                        break;
                    }
                    long before = System.currentTimeMillis();
                    try {
                        client.fastCall("fast");
                        long after = System.currentTimeMillis();
                        long cost = after - before;
                        fastHistogram.update(cost);
                    } catch (TException e) {
                        fastErrorCount.incrementAndGet();
                    }
                } else {
                    id = slowTotalCount.incrementAndGet();
                    if (id > slowMsgCount) {
                        slowEndTime = System.currentTimeMillis();
                        break;
                    }
                    long before = System.currentTimeMillis();
                    try {
                        client.slowCall("slow");
                        long after = System.currentTimeMillis();
                        long cost = after - before;
                        slowHistogram.update(cost);
                    } catch (TException e) {
                        slowErrorCount.incrementAndGet();
                    }
                }
            }


        }
    }
    @Test
    public void test() {}
}
