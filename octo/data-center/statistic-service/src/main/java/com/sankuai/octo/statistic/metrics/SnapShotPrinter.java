package com.sankuai.octo.statistic.metrics;


public class SnapShotPrinter {

    public static void print(Snapshot snapshot) {
        System.out.println("               min = " + snapshot.getMin());
        System.out.println("               max = " + snapshot.getMax());
        System.out.println("              mean = " + snapshot.getMean());
        System.out.println("            stddev = " + snapshot.getStdDev());
        System.out.println("            median = " + snapshot.getMedian());
        System.out.println("              75%% <= " + snapshot.get75thPercentile());
        System.out.println("              90%% <= " + snapshot.getValue(0.90));
        System.out.println("              95%% <= " + snapshot.get95thPercentile());
        System.out.println("              98%% <= " + snapshot.get98thPercentile());
        System.out.println("              99%% <= " + snapshot.get99thPercentile());
        System.out.println("            99.9%% <= " + snapshot.get999thPercentile());
    }
}
