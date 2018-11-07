package com.sankuai.meituan.config.util;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by lhmily on 07/26/2016.
 */
public class CronScheduler {
    private static Logger logger = LoggerFactory.getLogger(CronScheduler.class);

    private static Scheduler cronScheduler = null;

    static {
        try {
            cronScheduler = new StdSchedulerFactory().getScheduler();
            cronScheduler.start();
        } catch (Exception e) {
            logger.error("cannot get scheduler.", e);
        }
        // 在jvm退出时优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    cronScheduler.shutdown(true);
                } catch (Exception e) {
                    logger.error("cronScheduler cannot exit.", e);
                }
            }
        });
    }

    public static void scheduleJob(JobDetail job, Trigger trigger) {
        try {
            cronScheduler.scheduleJob(job, trigger);
        } catch (Exception e){
            logger.error("scheduleJob Fail,job:{},trigger:{}", job, trigger);
            logger.error("scheduleJob Fail.", e);
        }
    }
}
