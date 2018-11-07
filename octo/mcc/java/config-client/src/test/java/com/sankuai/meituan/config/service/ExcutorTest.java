package com.sankuai.meituan.config.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExcutorTest {
    private static ScheduledExecutorService testExecutor = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> testScheduledFuture;
    public static void main(String args[]) {

        testScheduledFuture = testExecutor.scheduleWithFixedDelay(new Runnable() {
            volatile int count = 0;

            @Override
            public void run() {
                System.out.println("start test");
                if (++ count > 10) {
                    ExcutorTest.testScheduledFuture.cancel(true);
                    ExcutorTest.testExecutor.shutdown();
                }
                System.gc();
            }
        }, 1, 1, TimeUnit.SECONDS);
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println(ExcutorTest.testScheduledFuture.isCancelled());
                System.out.println(ExcutorTest.testExecutor.isShutdown());
                System.gc();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
}
