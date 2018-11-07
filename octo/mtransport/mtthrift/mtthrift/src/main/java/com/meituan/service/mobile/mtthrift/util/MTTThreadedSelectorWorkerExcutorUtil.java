package com.meituan.service.mobile.mtthrift.util;

import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: YangXuehua
 * Date: 13-6-24
 * Time: 下午3:16
 * To change this template use File | Settings | File Templates.
 */
public class MTTThreadedSelectorWorkerExcutorUtil {

    public static ThreadPoolExecutor getWorkerExcutor(int min, int max, MTDefaultThreadFactory mtDefaultThreadFactory) {
        return new ThreadPoolExecutor(min, max, 30L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), mtDefaultThreadFactory);
    }

    public static ThreadPoolExecutor getWorkerExecutorWithQueue(int min, int max, int workQueueSize, MTDefaultThreadFactory mtDefaultThreadFactory) {
        ThreadPoolExecutor executor;
        if (workQueueSize <= 0) {
            executor = new ThreadPoolExecutor(min, max, 30L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), mtDefaultThreadFactory);
        } else {
            executor = new ThreadPoolExecutor(min, max, 30L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(workQueueSize), mtDefaultThreadFactory);
        }
        executor.prestartAllCoreThreads();
        return executor;
    }

    public static ThreadPoolExecutor getWorkerExecutorWithQueue(int min, int max, BlockingQueue<Runnable> queue,
            MTDefaultThreadFactory mtDefaultThreadFactory) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(min, max, 30L, TimeUnit.SECONDS,
                    queue, mtDefaultThreadFactory);
        executor.prestartAllCoreThreads();
        return executor;
    }
}
