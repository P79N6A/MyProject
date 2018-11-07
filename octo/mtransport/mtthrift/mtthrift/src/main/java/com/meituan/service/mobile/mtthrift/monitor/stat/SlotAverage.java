package com.meituan.service.mobile.mtthrift.monitor.stat;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: YangXuehua
 * Date: 13-8-16
 * Time: 下午2:08
 */
@Deprecated
public class SlotAverage extends Slot {
    private int periods = 10;

    private AtomicLong numAmount;
    private AtomicLong numCount;
    private LinkedList<Average> stack_count;

    public SlotAverage(int maxPeriods) {
        super();
        if (maxPeriods <= 2)
            maxPeriods = 2;
        this.periods = maxPeriods;
        this.numAmount = new AtomicLong(0L);
        this.numCount = new AtomicLong(0L);
        this.stack_count = new LinkedList<Average>();
    }

    public void destroy() {
        super.destroy();
    }

    public void addNum(long num) {
        this.numAmount.addAndGet(num);
        this.numCount.incrementAndGet();
    }

    @Override
    protected void pollCheck() {
        long count = this.numCount.getAndSet(0L);
        long amount = this.numAmount.getAndSet(0L);
        long ave = count > 0 ? amount / count : -1;

        Average average = new Average();
        average.setAverage(ave);
        average.setCount(count);
        average.setPeriod(secoundsPerPeriod);
        stack_count.offerFirst(average);
        if (stack_count.size() > periods)
            stack_count.removeLast();
    }

    private Average _getAverage(int indexFromLatest) {
        if (stack_count.size() > indexFromLatest && indexFromLatest >= 0) {
            return stack_count.get(indexFromLatest);
        }
        return null;
    }

    /**
     *
     * @param indexFromLatest
     *            0 最近一个周期的统计，1 上一个周期的统计
     * @return
     */
    public Average getAveragePeriodsAgo(int indexFromLatest) {
        if (indexFromLatest < 0)
            indexFromLatest = 0;
        return _getAverage(indexFromLatest);
    }

    public static void main(String... avgs) {
        LinkedList<Integer> list = new LinkedList<Integer>();
        list.offerFirst(1);
        list.offerFirst(2);
        list.offerFirst(3);
        list.offer(4);
        for (int i = 0; i < list.size(); i++)
            System.out.println(list.get(i));
    }

    public static class Average {
        private long count;
        private long average;
        private int period;

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public long getAverage() {
            return average;
        }

        public void setAverage(long average) {
            this.average = average;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        @Override
        public String toString() {
            return "Average{" +
                    "count=" + count +
                    ", average=" + average +
                    ", period=" + period +
                    '}';
        }
    }

}