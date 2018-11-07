package com.sankuai.mtthrift.testSuite.threadpool;

import org.apache.thrift.TException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-9-18
 * Time: 下午4:01
 */
public class EchoServiceImpl implements EchoService.Iface {

    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private static AtomicLong fastCount = new AtomicLong(0);
    private static AtomicLong slowCount = new AtomicLong(0);

    static {
        service.scheduleWithFixedDelay(new Runnable() {

            public void run() {
                try {
                    System.out.println("fast:" + fastCount.get() + ", slow:" + slowCount.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
    }


    public String slowCall(String username) throws TException {
        slowCount.incrementAndGet();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        slowCount.decrementAndGet();
        return username;
    }

    public String fastCall(String username) throws TException {
        fastCount.incrementAndGet();
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fastCount.decrementAndGet();
        return username;
    }

}
