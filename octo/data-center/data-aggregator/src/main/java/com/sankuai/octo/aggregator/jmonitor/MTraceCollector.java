package com.sankuai.octo.aggregator.jmonitor;

import com.meituan.jmonitor.collector.IJMonitorCollector;
import com.meituan.jmonitor.store.IJMonitorStore;
import com.sankuai.octo.aggregator.parser.common.MtraceParser;

public class MTraceCollector implements IJMonitorCollector {
    @Override
    public void doCollect(IJMonitorStore store) {
        long timeMillis = System.currentTimeMillis();
        store.store(this, "mtrace.count", MtraceParser.MTraceCount().get(), timeMillis);
        store.store(this, "mtrace.error.count", MtraceParser.MTraceErrorCount().get(), timeMillis);
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
