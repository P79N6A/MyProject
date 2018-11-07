package com.sankuai.octo.benchmark.thrift;

import com.sankuai.octo.benchmark.falcon.FalconReportUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-12-8
 * Time: 下午5:25
 */
public class EchoServiceImpl implements EchoService.Iface {

    public static final Logger logger = LoggerFactory.getLogger(EchoServiceImpl.class);
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
                    logger.info("TPS:" + tps);
                    FalconReportUtils.addItem("benchmark.mtthrift.TPS", String.valueOf(tps));
                    FalconReportUtils.report();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, timeWindow, TimeUnit.SECONDS);
    }

    @Override
    public String sendString(String str) throws TException {
        counter.addAndGet(1);
        return str;
    }

    @Override
    public ByteBuffer sendBytes(ByteBuffer bytes) throws TException {
        counter.addAndGet(1);
        return bytes;
    }

    @Override
    public List<Message> sendPojo(List<Message> msgList) throws TException {
        counter.addAndGet(1);
        Message msg = new Message().setId(1);
        return Collections.singletonList(msg);
    }

    public static long fibonacci(long n) {
        if (n <= 2) {
            return 1;
        } else {
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
    }
}
