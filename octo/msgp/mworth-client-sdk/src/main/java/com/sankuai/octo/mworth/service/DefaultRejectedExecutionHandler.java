package com.sankuai.octo.mworth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by yuexiaojun on 15/2/4.
 */
public class DefaultRejectedExecutionHandler implements RejectedExecutionHandler {

    private final Logger log = LoggerFactory.getLogger(DefaultRejectedExecutionHandler.class);

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        log.error("asyn save rejected error , max queue size {}, max thread pool size {}", executor.getQueue().size(), executor.getMaximumPoolSize());

    }
}
