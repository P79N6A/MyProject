package com.meituan.service.mobile.mtthrift.monitor.stat;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: YangXuehua
 * Date: 13-8-16
 * Time: 下午2:08
 */
@Deprecated
public class SlotCount extends Slot {
    private int periods = 10;

    private AtomicLong amount;
    private LinkedList<Long> stack_count;

    public SlotCount(int maxPeriods) {
        super();
        if (maxPeriods <= 2)
            maxPeriods = 2;
        this.periods = maxPeriods + 1;
        if (periods < 2)
            periods = 2;
        this.amount = new AtomicLong(0L);
        this.stack_count = new LinkedList<Long>();
    }

    public void destroy() {
        super.destroy();
    }

    public long inc() {
        return amount.incrementAndGet();
    }

    public long incBy(int step) {
        return amount.addAndGet(step);
    }

    public long desc() {
        return amount.decrementAndGet();
    }

    public long descBy(int step) {
        return amount.addAndGet(step * -1L);
    }

    public long getAndSet(long newValue) {
        return amount.getAndSet(newValue);
    }

    public long getNowValue() {
        return amount.longValue();
    }

    @Override
    protected void pollCheck() {
        stack_count.offerFirst(amount.longValue());
        if (stack_count.size() > periods + 1)
            stack_count.removeLast();
    }

    /**
     *
     * @param periodsLength
     *            最近多少个周期内的增长值
     * @return
     */
    public Increase getIncrease(int periodsLength) {
        if (stack_count.size() > periodsLength) {
            Increase increase = new Increase();
            increase.setIncrease(stack_count.getFirst().longValue() - stack_count.get(periodsLength).longValue());
            increase.setPeriod(secoundsPerPeriod * periodsLength);
            return increase;
        }
        return null;
    }

    public static class Increase {
        private long increase;
        private int period;

        public long getIncrease() {
            return increase;
        }

        public void setIncrease(long increase) {
            this.increase = increase;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }
    }

}