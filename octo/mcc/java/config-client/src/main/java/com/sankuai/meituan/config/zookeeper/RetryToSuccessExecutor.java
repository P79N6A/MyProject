package com.sankuai.meituan.config.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RetryToSuccessExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryToSuccessExecutor.class);
    private ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1);
    private Runnable runner;
    private ScheduledFuture<?> retryScheduledFuture;

    public RetryToSuccessExecutor(final Runnable runner, int periodSeconds) {
        this.runner = runner;
        try {
            this.runner.run();
            LOGGER.info(String.format("执行[%s]成功!", runner.toString()));
        } catch (Exception e) {
            LOGGER.error(String.format("执行[%s]失败,会一直重试到成功为止", runner.toString()), e);
        }
        this.retryScheduledFuture = this.retryExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    RetryToSuccessExecutor.this.runner.run();
                    shutdown();
                    LOGGER.info(String.format("执行[%s]成功!", runner.toString()));
                } catch (Exception e) {
                    LOGGER.info(String.format("执行[%s]失败,会一直重试到成功为止", runner.toString()), e);
                }
            }
        }, periodSeconds, periodSeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        RetryToSuccessExecutor.this.retryScheduledFuture.cancel(true);
        RetryToSuccessExecutor.this.retryExecutor.shutdown();
    }
}
