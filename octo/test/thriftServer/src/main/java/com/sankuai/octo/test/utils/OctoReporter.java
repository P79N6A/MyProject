package com.sankuai.octo.test.utils;

import com.codahale.metrics.*;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class OctoReporter extends ScheduledReporter {

    public OctoReporter(MetricRegistry registry, TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, "octo-reporter", MetricFilter.ALL, rateUnit, durationUnit);
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        //System.out.println(gauges);
        //System.out.println(counters);

        if (!histograms.isEmpty()) {
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                System.out.println(entry.getKey());
                Histogram histogram = entry.getValue();
                System.out.println("             count = " + histogram.getCount());
                Snapshot snapshot = histogram.getSnapshot();
                System.out.println("               min = " + snapshot.getMin());
                System.out.println("               max = " + snapshot.getMax());
                System.out.println("              mean = " + snapshot.getMean());
                System.out.println("            stddev = " + snapshot.getStdDev());
                System.out.println("            median = " + snapshot.getMedian());
                System.out.println("              75%% <= " + snapshot.get75thPercentile());
                System.out.println("              95%% <= " + snapshot.get95thPercentile());
                System.out.println("              98%% <= " + snapshot.get98thPercentile());
                System.out.println("              99%% <= " + snapshot.get99thPercentile());
                System.out.println("            99.9%% <= " + snapshot.get999thPercentile());
            }
            System.out.println();
        }

        //System.out.println(meters);
        //System.out.println(timers);
    }
}