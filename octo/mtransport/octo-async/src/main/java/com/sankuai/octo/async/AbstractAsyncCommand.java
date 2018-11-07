package com.sankuai.octo.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步命令
 * （采用原生的异步实现并发）
 * Created by wangchao23 on 2016-07-18.
 */
public abstract class AbstractAsyncCommand<T> implements Command<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAsyncCommand.class);
    //CPU核数
    private static int cpuCoreNum = Runtime.getRuntime().availableProcessors();
    //线程兜底异常处理器
    private static Thread.UncaughtExceptionHandler asyncCommandExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOG.error("Uncaught Exception from asyncCommandExecutor. threadName={}",
                    t.getName(), e);
        }
    };

    //线程工厂
    private static ThreadFactory asyncCommandThreadFactory =
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "AsyncCommandThreadPool-"
                            + threadNumber.getAndIncrement());
                    t.setUncaughtExceptionHandler(asyncCommandExceptionHandler);
                    return t;
                }
            };

    //专门用来做“调用链组装”和“模型转换”的执行器（只有纯CPU操作,所以采用固定线程池大小)
    protected static final Executor asyncCommandExecutor = Executors.newFixedThreadPool(cpuCoreNum, asyncCommandThreadFactory);
}
