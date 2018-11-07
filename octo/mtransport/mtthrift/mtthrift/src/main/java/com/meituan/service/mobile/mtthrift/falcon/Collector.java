package com.meituan.service.mobile.mtthrift.falcon;

import com.meituan.service.mobile.mtthrift.falcon.model.WorkerThreadMonitor;
import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import com.sankuai.inf.octo.mns.falcon.FalconCollect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-2-2
 * Time: 上午11:49
 */
public class Collector {
    private static final Logger LOG = LoggerFactory.getLogger(Collector.class);
    private static final int STEP = 60;
    private static final Map<String, WorkerThreadMonitor> workerThreadMonitorMap = new ConcurrentHashMap<String, WorkerThreadMonitor>();
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new MTDefaultThreadFactory("mtthrift-falcon-monitor"));

    static {
        service.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    workerThreadReport();
                } catch (Exception e) {
                    LOG.debug(e.getMessage(), e);
                }
            }
        }, 0, STEP, TimeUnit.SECONDS);
    }

    private Collector() {
    }

    public static Map<String, WorkerThreadMonitor> getWorkerThreadMonitorMap() {
        return workerThreadMonitorMap;
    }

    /**
     * 执行工作线程池状态上报
     */
    public static void workerThreadReport() {
        for (Map.Entry<String, WorkerThreadMonitor> each : workerThreadMonitorMap.entrySet()) {
            WorkerThreadMonitor workerThreadMonitor = each.getValue();
            FalconCollect.setItem(Metric.METRIC_ACTIVETHREADNUM, workerThreadMonitor.getTags(), String.valueOf(workerThreadMonitor.getActiveThreadNum()));
            FalconCollect.setItem(Metric.METRIC_POOLSIZE, workerThreadMonitor.getTags(), String.valueOf(workerThreadMonitor.getPoolSize()));
            FalconCollect.setItem(Metric.METRIC_WORKQUEUESIZE, workerThreadMonitor.getTags(), String.valueOf(workerThreadMonitor.getWorkQueueSize()));
        }
    }
}
