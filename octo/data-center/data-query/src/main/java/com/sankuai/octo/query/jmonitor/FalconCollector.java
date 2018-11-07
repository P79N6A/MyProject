package com.sankuai.octo.query.jmonitor;

import com.meituan.jmonitor.collector.IJMonitorCollector;
import com.meituan.jmonitor.store.IJMonitorStore;
import com.sankuai.octo.query.falconData.FalconHistoryData;
import com.sankuai.octo.query.falconData.FalconLastData;

/**
 *   @see com.sankuai.octo.statistic.collector.HBaseReadCollector
 */
public class FalconCollector implements IJMonitorCollector {
    @Override
    public void doCollect(IJMonitorStore store) {
        long time = System.currentTimeMillis();
        int historyCount = FalconHistoryData.getAndResetReadCount();
        store.store(this, "falcon.history.read.count", historyCount, time);

        int lastCount = FalconLastData.getAndResetReadCount();
        store.store(this, "falcon.last.read.count", lastCount, time);

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
