package com.sankuai.octo.statistic.collector;

import com.meituan.jmonitor.collector.IJMonitorCollector;
import com.meituan.jmonitor.store.IJMonitorStore;
import com.sankuai.octo.statistic.model.StatGroup;
import com.sankuai.octo.statistic.model.StatRange;
import com.sankuai.octo.statistic.util.HBaseClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HBaseWriteCollector implements IJMonitorCollector {


    @Override
    public void doCollect(IJMonitorStore store) {
        long time = System.currentTimeMillis();
        Map<StatRange, Map<StatGroup, AtomicInteger>> writeCountMap = HBaseClient.getAndResetWriteCount();
        int totalCount = 0;
        for (Map.Entry<StatRange, Map<StatGroup, AtomicInteger>> rangeMapEntry : writeCountMap.entrySet()) {
            StatRange range = rangeMapEntry.getKey();
            Map<StatGroup, AtomicInteger> counterMap = rangeMapEntry.getValue();
            int rangeCount = 0;
            for (Map.Entry<StatGroup, AtomicInteger> groupEntry : counterMap.entrySet()) {
                StatGroup group = groupEntry.getKey();
                String key = "hbase.write." + range + "." + group + ".count";
                AtomicInteger count = groupEntry.getValue();
                store.store(this, key, count.get(), time);
                rangeCount += count.get();
            }
            String rangeKey = "hbase.write." + range + ".groupAll.count";
            store.store(this, rangeKey, rangeCount, time);
            totalCount += rangeCount;
        }
        store.store(this, "hbase.write.count", totalCount, time);
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