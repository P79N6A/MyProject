package com.sankuai.meituan.config.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskUtil {
    private static final Logger LOG = LoggerFactory.getLogger(TaskUtil.class);

    private static final Executor thread = Executors.newFixedThreadPool(1);

    public static void singletonExecute(Runnable runnable) {
        try {
            if (Common.isTaskIP()) {
                LOG.info("current machine executes singleton task");
                thread.execute(runnable);
            } else {
                LOG.info("current machine is not the singleton task executor");
            }
        } catch (Exception e) {
            LOG.error("execute task error!", e);
        }
    }
}
