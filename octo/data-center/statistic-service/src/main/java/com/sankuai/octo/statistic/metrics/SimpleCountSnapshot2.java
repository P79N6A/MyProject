package com.sankuai.octo.statistic.metrics;


import com.sankuai.sgagent.thrift.model.PerfCostData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by zava on 15/9/23.
 * 耗时与次数 对应的快照数据
 */
public class SimpleCountSnapshot2 extends Snapshot {
    private static final Logger logger = LoggerFactory.getLogger(SimpleCountSnapshot2.class);

    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private final int[] values;
    private final long[] counts;
    private final double[] normWeights;
    private final double[] quantiles;
    private long max = 0;
    private long count = 0;    //总数

    /**
     * @param values TreeMap <时间,个数>
     * @param max    最大耗时 毫秒
     */
    SimpleCountSnapshot2(final TreeMap<Integer, Long> values, long max) {
        this.max = max;
        int size = values.size();
        this.values = new int[size];
        this.counts = new long[size];
        this.normWeights = new double[size];
        this.quantiles = new double[size];

        if (size > 0) {
            long sumCount = 0;

            Iterator<Map.Entry<Integer, Long>> iter = values.entrySet().iterator();
            int i = 0;
            while (iter.hasNext()) {
                Map.Entry<Integer, Long> entry = iter.next();
                sumCount += entry.getValue();
                this.values[i] = entry.getKey();
                this.counts[i] = entry.getValue();
                i++;
            }
            this.count = sumCount;

            this.normWeights[0] = (double) values.get(this.values[0]) / sumCount;
            for (i = 1; i < values.size(); i++) {
                this.normWeights[i] = (double) values.get(this.values[i]) / sumCount;
                this.quantiles[i] = this.quantiles[i - 1] + this.normWeights[i - 1];
            }
        }
    }

    @Override
    public long[] getValues() {
        long[] destLong = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            destLong[i] = values[i];
        }
        return destLong;
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public long getMax() {
        if (values.length == 0) {
            return 0;
        }
        return this.max;
    }

    @Override
    public double getMean() {
        if (values.length == 0) {
            return 0;
        }

        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += values[i] * normWeights[i];
        }
        return sum;
    }

    @Override
    public long getMin() {
        if (values.length == 0) {
            return 0;
        }
        return values[0];
    }

    @Override
    public double getValue(double quantile) {
        if (quantile < 0.0 || quantile > 1.0 || Double.isNaN(quantile)) {
            throw new IllegalArgumentException(quantile + " is not in [0..1]");
        }

        if (values.length == 0) {
            return 0.0;
        }

        int posx = Arrays.binarySearch(quantiles, quantile);
        if (posx < 0)
            posx = ((-posx) - 1) - 1;

        if (posx < 1) {
            return values[0];
        }

        if (posx >= values.length) {
            return values[values.length - 1];
        }

        return values[posx];
    }

    @Override
    public double getStdDev() {
        if (values.length <= 1) {
            return 0;
        }

        final double mean = getMean();
        double variance = 0;

        for (int i = 0; i < values.length; i++) {
            final double diff = values[i] - mean;
            variance += normWeights[i] * diff * diff;
        }
        return Math.sqrt(variance);
    }

    @Override
    public void dump(OutputStream output) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(output, UTF_8))) {
            out.printf("%d%n", count);
            for (int i = 0; i < values.length; i++) {
                long value = values[i];
                long count = counts[i];
                out.printf("%d%n", value);
                out.printf("%d%n", count);
            }
        }

    }

    public List<PerfCostData> getCostDataList() {
        if (values == null || values.length == 0) {
            return Collections.emptyList();
        } else {
            //  values 与 counts 的length 必须保持一致
            try {
                List<PerfCostData> costDataList = new ArrayList<>();
                for (int i = 0; i < values.length; i++) {
                    costDataList.add(new PerfCostData(values[i], counts[i]));
                }
                return costDataList;
            } catch (Exception e) {
                logger.error("getCostDataList Fail,valuesLength:{},countsLength:{}", values.length, counts.length, e);
                return Collections.emptyList();
            }
        }
    }
}
