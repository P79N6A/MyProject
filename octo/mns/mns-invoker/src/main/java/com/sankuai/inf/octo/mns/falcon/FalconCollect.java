package com.sankuai.inf.octo.mns.falcon;


import com.sankuai.inf.octo.mns.util.CommonUtil;
import com.sankuai.inf.octo.mns.util.ScheduleTaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-1-29
 * Time: 下午3:08
 */
public class FalconCollect {

    private static final Logger LOG = LoggerFactory.getLogger(ReportUtil.class);
    private static final int STEP = 60;
    private static ConcurrentHashMap<String, Item> map = new ConcurrentHashMap<String, Item>();

    private FalconCollect() {

    }

    static {
        String hostName = ReportUtil.getLocalHostName();

        //if the ENDPOINT(hostname) cannot be identified， the statistical data will be not uploaded to falcon.
        if (!CommonUtil.isBlankString(hostName) && !ReportUtil.UNKNOWN.equals(hostName)) {
            CommonUtil.mnsCommonSchedule.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        doCollect();
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }, 0, STEP, TimeUnit.SECONDS);
        }
    }

    /**
     * 执行收集任务
     */
    private static void doCollect() {

        long time = System.currentTimeMillis();
        Map<String, Item> data = getAndReset();
        List<Counter> list = new ArrayList<Counter>();

        for (Map.Entry<String, Item> each : data.entrySet()) {
            Item item = each.getValue();
            boolean enableValue = item.isEnableValue();
            long count = item.getCount();
            long sumtime = item.getSumtime();
            long meantime = (count > 0 && sumtime > 0) ? sumtime / count : -1;

            Counter counter;
            if (item.enableMaxValue) {
                // set the max value
                counter = new Counter(item.getMetric(), item.getTags(), time, String.valueOf(item.getMaxValue()));
            } else if (meantime >= 0) {
                //对应meantime时间类型的监控项
                counter = new Counter(item.getMetric(), item.getTags(), time, String.valueOf(meantime));
            } else if (enableValue) {
                //对应直接设置上报值类型的监控项
                counter = new Counter(item.getMetric(), item.getTags(), time, item.getValue());
            } else if (item.isEnableRateCount()) {
                //上报比例
                int totalCount = item.getCount();
                float rate = 0 < totalCount ? (item.getRateCount() * 1.0f / totalCount * 100) : 0.0f;
                counter = new Counter(item.getMetric(), item.getTags(), time, String.valueOf(rate));
            } else {
                //对应++类型的监控项，比如：异常统计
                counter = new Counter(item.getMetric(), item.getTags(), time, String.valueOf(item.getCount()));
            }

            list.add(counter);
        }
        if (!list.isEmpty())
            ReportUtil.doIOWrite(list);
    }

    /**
     * 返回map的副本并重置map，保留map中的key
     *
     * @return
     */
    private static Map<String, Item> getAndReset() {
        Map<String, Item> cloned = map;
        synchronized (FalconCollect.class) {
            map = new ConcurrentHashMap<String, Item>();
            for (Map.Entry<String, Item> entry : cloned.entrySet()) {
                Item item = new Item();
                item.metric = entry.getValue().metric;
                item.tags = entry.getValue().tags;
                map.put(item.metric + item.tags, item);
            }
        }
        return cloned;
    }

    /**
     * 根据metric+tags取出Item，若为空则创建
     *
     * @param metric
     * @param tags
     * @return
     */
    private static Item makeSureExist(String metric, String tags) {
        Item item = map.get(metric + tags);
        if (null == item) {
            synchronized (FalconCollect.class) {
                item = map.get(metric + tags);
                if (null == item) {
                    item = new Item();
                    item.metric = metric;
                    item.tags = tags;
                    map.put(metric + tags, item);
                }
            }
        }
        return item;
    }

    /**
     * 设置某个Item的值，value为可以转化为int、float的string类型
     *
     * @param metric
     * @param tags
     * @param value
     */
    public static void setItem(String metric, String tags, String value) {
        Item item = makeSureExist(metric, tags);
        item.enableValue = true;
        item.value = value;
    }

    /**
     * 增加某个Item的耗时，计算meantime
     *
     * @param metric
     * @param tags
     * @param mills
     */
    public static void addItem(String metric, String tags, long mills) {
        Item item = makeSureExist(metric, tags);
        item.sumtime.addAndGet(mills);
        item.count.incrementAndGet();
    }

    /**
     * 增加某个Item的统计值
     *
     * @param metric
     * @param tags
     */
    public static void addItem(String metric, String tags) {
        Item item = makeSureExist(metric, tags);
        item.count.incrementAndGet();
    }

    public static void setRate(String metric, String tags, boolean isIncRateCount) {
        Item item = makeSureExist(metric, tags);
        item.enableRateCount = true;
        item.count.incrementAndGet();
        if (isIncRateCount) {
            item.rateCount.incrementAndGet();
        }
    }

    public static void setMax(String metric, String tags, long maxValue) {
        Item item = makeSureExist(metric, tags);
        item.enableMaxValue = true;
        item.setMaxValue(maxValue);
    }

    private static class Item {
        String metric;
        String tags;
        AtomicLong sumtime = new AtomicLong();
        AtomicInteger count = new AtomicInteger();

        boolean enableRateCount = false;
        AtomicInteger rateCount = new AtomicInteger();

        boolean enableValue = false; // true:对应set操作，标记是否取value值
        String value;
        long maxValue = 0L;

        boolean enableMaxValue = false;

        private Item() {
        }

        public String getMetric() {
            return metric;
        }

        public String getTags() {
            return tags;
        }

        public long getSumtime() {
            return sumtime.get();
        }

        public int getCount() {
            return count.get();
        }

        public boolean isEnableValue() {
            return enableValue;
        }

        public String getValue() {
            return value;
        }

        public boolean isEnableRateCount() {
            return enableRateCount;
        }

        public int getRateCount() {
            return rateCount.get();
        }

        public long getMaxValue() {
            return maxValue;
        }

        public synchronized void setMaxValue(long maxValue) {
            if (maxValue > this.maxValue) {
                this.maxValue = maxValue;
            }

        }
    }
}
