package com.sankuai.octo.statistic.model;

import com.sankuai.octo.statistic.helper.TimeProcessor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.concurrent.atomic.AtomicLong;

public class TimeCount {
    private int sencond;
    private AtomicLong count;

    public TimeCount() {
        this.sencond = getCurrentSencond();
        this.count = new AtomicLong(0);
    }

    public long getSencond() {
        return sencond;
    }

    public void setSencond(int sencond) {
        this.sencond = sencond;
    }

    public AtomicLong getCount() {
        return count;
    }

    public long addAndGet(int size) {
        int second = getCurrentSencond();
        if (!isNow(second)) {
            reset(second);
        }
        return count.addAndGet(size);
    }

    public void setCount(AtomicLong count) {
        this.count = count;
    }

    public void reset() {
        int second = getCurrentSencond();
        reset(second);
    }

    private int getCurrentSencond() {
        int time = (int) (System.currentTimeMillis() / 1000L);
        return TimeProcessor.getMinuteStart(time);
    }

    private boolean isNow(int second) {
        return second == this.getSencond() ? true : false;
    }


    private void reset(int second) {
        this.sencond = second;
        this.count.set(0);
    }

    public String toString(){
       return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
