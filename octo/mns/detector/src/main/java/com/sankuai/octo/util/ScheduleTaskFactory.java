package com.sankuai.octo.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-4-20
 * Time: 下午6:05
 */
public class ScheduleTaskFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public ScheduleTaskFactory(String threadPoolName) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = threadPoolName + "-"
                + poolNumber.getAndIncrement()
                + "-thread-";
    }

    public Thread newThread(Runnable r) {
        //创建的线程以“S-N-thread-M”命名，S是Service接口名，N是该工厂的序号，M是线程号
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(), 0);

        t.setDaemon(true);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}