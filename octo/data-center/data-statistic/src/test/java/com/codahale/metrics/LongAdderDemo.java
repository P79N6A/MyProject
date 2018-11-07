package com.codahale.metrics;

import com.sankuai.octo.statistic.metrics.LongAdder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zava on 15/9/28.
 * 多线程环境下 LongAdder 比 AtomicLong 表现优异
 */
public class LongAdderDemo {
    public static void main(String[] args) {

        LongAdder longAdder = new LongAdder();
        longAdder.add(5);
        longAdder.add(-5);
        longAdder.increment();
        longAdder.decrement();
        longAdder.increment();
        System.out.println("longAdder.intValue() = " + longAdder.intValue());
        final long sum = longAdder.sum();
        System.out.println("sum = " + sum);
        System.out.println("longAdder.intValue() = " + longAdder.intValue());

        final long sumThenReset = longAdder.sumThenReset();
        System.out.println("sumThenReset = " + sumThenReset);
        System.out.println("longAdder.intValue() = " + longAdder.intValue());
        testAtomicLong();
        testLongAdder();
        testThreadAtomicLong();
        testThreadLongAdder();


    }

    private static void testThreadAtomicLong() {
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        final AtomicLong atomicLong = new AtomicLong();
        Runnable runnableAtomicLong = new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 100000; i++) {
                atomicLong.incrementAndGet();
//                }
            }
        };

        for (int i = 0; i < 100000; i++) {
            executorService.submit(runnableAtomicLong);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(2000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("并发AtomicLong耗时:" + (System.currentTimeMillis() - start)
                + "ms," + atomicLong.intValue());
    }

    private static void testThreadLongAdder() {
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        final LongAdder longAdder = new LongAdder();
        Runnable runnableLongAddr = new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < 100000; i++) {
                longAdder.increment();
//                }
            }
        };
        for (int i = 0; i < 100000; i++) {
            executorService.submit(runnableLongAddr);
        }


        executorService.shutdown();
        // 等待子线程结束，再继续执行下面的代码
        try {
            executorService.awaitTermination(2000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("并发LongAdder耗时:" + (System.currentTimeMillis() - start)
                + "ms," + longAdder.intValue());
    }


    private static void testAtomicLong() {
        long start = System.currentTimeMillis();
        final AtomicLong atomicLong = new AtomicLong();
        for (int i = 0; i < 10000000; i++) {
            atomicLong.incrementAndGet();
        }
        System.out.println("AtomicLong耗时:" + (System.currentTimeMillis() - start)
                + "ms," + atomicLong.intValue());
    }

    private static void testLongAdder() {
        long start = System.currentTimeMillis();
        final LongAdder longAdder = new LongAdder();
        for (int i = 0; i < 10000000; i++) {
            longAdder.increment();
        }

        System.out.println("LongAdder耗时:" + (System.currentTimeMillis() - start)
                + "ms," + longAdder.intValue());
    }


}
