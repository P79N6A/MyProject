package com.sankuai.inf.octo.mns.falcon;

import com.sankuai.inf.octo.mns.util.CommonUtil;
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


public class Falcon {

    private static final Logger LOG = LoggerFactory.getLogger(ReportUtil.class);
    private static final int STEP = 60;
    private static ConcurrentHashMap<String, Falcon.Item> map = new ConcurrentHashMap<String, Falcon.Item>();
    private static final ScheduledExecutorService scheduledFalconService = Executors.newScheduledThreadPool(1);

    private Falcon() {

    }

    static {
        String hostName = ReportUtil.getLocalHostName();

        //if the ENDPOINT(hostname) cannot be identified， the statistical data will be not uploaded to falcon.
        if (!CommonUtil.isBlankString(hostName) && !ReportUtil.UNKNOWN.equals(hostName)) {
            scheduledFalconService.scheduleWithFixedDelay(new Runnable() {
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
        Map<String, Falcon.Item> data = getAndReset();
        List<Counter> list = new ArrayList<Counter>();

        for (Map.Entry<String, Falcon.Item> each : data.entrySet()) {
            Falcon.Item item = each.getValue();

            Counter counter;

            counter = new Counter(item.getMetric(), item.getTags(), time, String.valueOf(item.getWatcherCost()));


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
    private static Map<String, Falcon.Item> getAndReset() {
        Map<String, Falcon.Item> cloned = map;
        synchronized (Falcon.class) {
            map = new ConcurrentHashMap<String, Falcon.Item>();
            for (Map.Entry<String, Falcon.Item> entry : cloned.entrySet()) {
                Falcon.Item item = new Falcon.Item();
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
    private static Falcon.Item makeSureExist(String metric, String tags) {
        Falcon.Item item = map.get(metric + tags);
        if (null == item) {
            synchronized (Falcon.class) {
                item = map.get(metric + tags);
                if (null == item) {
                    item = new Falcon.Item();
                    item.metric = metric;
                    item.tags = tags;
                    map.put(metric + tags, item);
                }
            }
        }
        return item;
    }

    public static void setMaxItem(String metric, String tags, long mills) {
        Falcon.Item item = makeSureExist(metric, tags);
        //mnsc的watcher process是单线程，不需要lock进行写map同步措施
        if (item.getWatcherCost() < mills) {
            item.watcher_cost.getAndSet(mills);
        }
    }


    private static class Item {
        String metric;
        String tags;

        AtomicLong watcher_cost = new AtomicLong(0);

        private Item() {
        }

        public String getMetric() {
            return metric;
        }

        public String getTags() {
            return tags;
        }

        public long getWatcherCost() {return watcher_cost.get();}
    }
}
