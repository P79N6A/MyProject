package com.sankuai.octo.jmonitor;

import com.meituan.jmonitor.collector.IJMonitorCollector;
import com.meituan.jmonitor.store.IJMonitorStore;
import com.sankuai.octo.statistic.util.Falcon;

public class FalconCollector implements IJMonitorCollector {
    @Override
    public void doCollect(IJMonitorStore store) {
        long time = System.currentTimeMillis();
        int readCount = Falcon.getAndResetWriteCount();
        store.store(this, "falcon.write.count", readCount, time);
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
