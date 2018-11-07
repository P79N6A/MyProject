package com.sankuai.octo.benchmark.service;


import com.sankuai.octo.benchmark.falcon.FalconReportUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-12-30
 * Time: 上午10:20
 */
public class EchoServiceImpl implements EchoService {

    private static volatile AtomicLong counter = new AtomicLong(0);
    private static long timeWindow = 10;
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    static {
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    long tps = counter.get() / timeWindow;
                    counter.set(0);
                    System.out.println("TPS:" + tps);
                    FalconReportUtils.addItem("benchmark.pigeon.TPS", String.valueOf(tps));
                    FalconReportUtils.report();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, timeWindow, TimeUnit.SECONDS);
    }

    public String sendString(String str) {
        counter.addAndGet(1);
        return str;
    }

    public ByteBuffer sendBytes(ByteBuffer bytes) {
        counter.addAndGet(1);
        return bytes;
    }

    public List<Message> sendPojo(List<Message> msgList) {
        counter.addAndGet(1);
        return msgList;
    }

    public static long fibonacci(long n){
        if(n <= 2){
            return 1;
        }else{
            return fibonacci(n-1) + fibonacci(n-2);
        }
    }
}
