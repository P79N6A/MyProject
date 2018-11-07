package com.sankuai.octo.statistic.metrics;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zava on 15/9/21.
 * <p/>
 * UniformReservoir 会丢失数据,导致 样本集过少,不能准确反映状况
 */
public class SimpleCountHistogramTest {
    public static void calcSize(Object o) {
        long memShallow = com.candybon.memory.MemoryObserver.shallowSizeOf(o);
        long memDeep = com.candybon.memory.MemoryObserver.deepSizeOf(o);
        System.out.printf("%s, shallow=%d, deep=%d%n", o.getClass().getSimpleName(), memShallow, memDeep);
    }

    @Test
    public void testTime() {
        long start = System.currentTimeMillis();
        SimpleCountHistogram histogram = new SimpleCountHistogram();
        for (int i = 0; i < 10000; i++) {
            print(histogram);
        }
//        calcSize(histogram);
        System.out.println("耗时:" + (System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void testSimpleHistogram() {
        SimpleCountHistogram histogram = new SimpleCountHistogram();
        print(histogram);
        SnapShotPrinter.print(histogram.getSnapshot());
    }

    @Test
    public void testThreadSimpleHistogram() {
        SimpleCountHistogram histogram = new SimpleCountHistogram();
        SimpleCountHistogram histogram2 = new SimpleCountHistogram();
        printThread(histogram, histogram2);
        SnapShotPrinter.print(histogram.getSnapshot());
        SnapShotPrinter.print(histogram2.getSnapshot());
    }


    private void print(SimpleCountHistogram gram) {
        for (int i = 0; i < 100000; i++) {
            int cost = (int) (Math.random() * 20);
            gram.update(cost);
        }
    }

    private void printThread(final SimpleCountHistogram gram, SimpleCountHistogram gram2) {
        ExecutorService poolExecutor = Executors.newFixedThreadPool(1000);
        int cost;

        for (int i = 0; i < 10000; i++) {
            if (i % 9999 == 0) {
                //极少数 在30s 以上
                cost = (int) (Math.random() * 1000 + 30000);
            } else if (i % 999 == 0) {
                //少部分在 40~50ms
                cost = (int) (Math.random() * 10);
            } else if (i % 2 == 0) {
                //50% 在20~30ms
                cost = (int) (Math.random() * 10 + 20);
            } else if (i % 3 == 0) {
                //少部分在 40~50ms
                cost = (int) (Math.random() * 10 + 40);
            } else {
                //剩下的随机分布在1s~2s之间
                cost = (int) (Math.random() * 1000 + 1000);
            }
            gram2.update(cost);
            final int cost1 = cost;
            poolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    gram.update(cost1);
                }
            });
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDumpInit() {
        SimpleCountHistogram gram = new SimpleCountHistogram();

        gram.update(999999);

        //50
        for (int i = 0; i < 50000; i++) {
            int cost = (int) (Math.random() * 100);
            gram.update(cost);
        }
        //75
        for (int i = 0; i < 25000; i++) {
            int cost = (int) (Math.random() * 3000) + 1000;
            gram.update(cost);
        }
        //90
        for (int i = 0; i < 15000; i++) {
            int cost = (int) (Math.random() * 5000) + 5000;
            gram.update(cost);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            gram.dump(stream);
            SimpleCountHistogram histogram = new SimpleCountHistogram();
            histogram.init(new ByteArrayInputStream(stream.toByteArray()));
            Assert.assertEquals(gram.getCount(), histogram.getCount());
            Snapshot srcSnapshot = gram.getSnapshot();
            Snapshot targSnapshot = histogram.getSnapshot();

            Assert.assertEquals(gram.getCount(), histogram.getCount());
            Assert.assertEquals(srcSnapshot.getMax(), targSnapshot.getMax());
            Assert.assertEquals(srcSnapshot.getMin(), targSnapshot.getMin());
            Assert.assertEquals(srcSnapshot.get75thPercentile(), targSnapshot.get75thPercentile(), 0.00001f);
            Assert.assertEquals(srcSnapshot.getMedian(), targSnapshot.getMedian(), 0.00001f);
            Assert.assertEquals(srcSnapshot.get99thPercentile(), targSnapshot.get99thPercentile(), 0.00001f);
            Assert.assertEquals(srcSnapshot.getStdDev(), targSnapshot.getStdDev(), 0.00001f);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
