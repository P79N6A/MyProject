package com.sankuai.octo.aggregator.jmonitor;

import com.meituan.jmonitor.collector.IJMonitorCollector;
import com.meituan.jmonitor.store.IJMonitorStore;
import com.sankuai.octo.aggregator.parser.DataParser;
import com.sankuai.octo.aggregator.parser.MetricParser;
import com.sankuai.octo.aggregator.parser.common.CommonLogParser;
import com.sankuai.octo.aggregator.parser.common.SlowQueryParser;
import com.sankuai.octo.aggregator.sender;

public class AsyncQueueCollector implements IJMonitorCollector {
    @Override
    public void doCollect(IJMonitorStore store) {
        long timeMillis = System.currentTimeMillis();

        int DataParserLength = DataParser.asyncTraceLogParser().executor().getQueue().size();
        int MetricParserLength = MetricParser.listCacheLen();
        int MetricSenderLength = MetricParser.asyncMetricSender().executor().getQueue().size();
        int SlowLogParserLength = SlowQueryParser.asyncSlowLogParser().executor().getQueue().size();
        int CommLogParserLength = CommonLogParser.asyncCommLogParser().executor().getQueue().size();
        int PerfQueueLength = sender.queue().size();
        int PerfSenderLength = sender.asyncSender().executor().getQueue().size();

        store.store(this, "dataParser.length", DataParserLength, timeMillis);
        store.store(this, "metricParser.length", MetricParserLength, timeMillis);
        store.store(this, "metricSender.length", MetricSenderLength, timeMillis);
        store.store(this, "slowLogParser.length", SlowLogParserLength, timeMillis);
        store.store(this, "commLogParser.length", CommLogParserLength, timeMillis);
        store.store(this, "perfQueue.length", PerfQueueLength, timeMillis);
        store.store(this, "perfSender.length", PerfSenderLength, timeMillis);
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
