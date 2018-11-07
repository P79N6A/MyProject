/*
package com.sankuai.octo.jmonitor;

import com.meituan.jmonitor.collector.IJMonitorCollector;
import com.meituan.jmonitor.store.IJMonitorStore;
import com.sankuai.octo.statistic.exporter.FalconExporter;
import com.sankuai.octo.statistic.exporter.HBaseExporter;

*/
/**
 * Created by wujinwu on 15/12/28.
 *//*

public class AsyncQueueCollector implements IJMonitorCollector {
    @Override
    public void doCollect(IJMonitorStore store) {
        long timeMillis = System.currentTimeMillis();
//        int metricProcessorLength = MetricProcessor.metricExecutor().executor().getQueue().size();
//        store.store(this, "metric.processor.length", metricProcessorLength, timeMillis);
        int falconExporterLength = FalconExporter.queue().size();
        store.store(this, "falcon.exporter.length", falconExporterLength, timeMillis);
        int hbaseQueueLength = HBaseExporter.queue().size();
        store.store(this, "hbase.queue.length", hbaseQueueLength, timeMillis);
//        int hbaseExporterLength = HBaseExporter.putExecutor().executor().getQueue().size();
//        store.store(this, "hbase.exporter.length", hbaseExporterLength, timeMillis);
//        int statExporterLength = DefaultExporterProxy.statExporter().executor().getQueue().size();
//        store.store(this, "stat.exporter.length", statExporterLength, timeMillis);
//        int tagsExporterLength = DefaultExporterProxy.tagExporter().executor().getQueue().size();
//        store.store(this, "tags.exporter.length", tagsExporterLength, timeMillis);
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
*/
