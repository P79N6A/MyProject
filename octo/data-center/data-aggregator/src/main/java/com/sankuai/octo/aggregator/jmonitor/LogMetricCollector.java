package com.sankuai.octo.aggregator.jmonitor;

import com.meituan.jmonitor.collector.IJMonitorCollector;
import com.meituan.jmonitor.store.IJMonitorStore;
import com.sankuai.octo.aggregator.util.LogMetricCounter;

public class LogMetricCollector implements IJMonitorCollector {
    @Override
    public void doCollect(IJMonitorStore store) {
        long timeMillis = System.currentTimeMillis();
        store.store(this, "sgModule.day.count", LogMetricCounter.perfDayCounter(), timeMillis);
        store.store(this, "sgModule.hour.count", LogMetricCounter.perfHourCounter(), timeMillis);
    }

    @Override
    public String prefixkey() {
        return "";
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
