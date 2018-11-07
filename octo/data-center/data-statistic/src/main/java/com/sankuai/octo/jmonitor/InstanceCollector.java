/*
package com.sankuai.octo.jmonitor;

import com.meituan.jmonitor.collector.IJMonitorCollector;
import com.meituan.jmonitor.store.IJMonitorStore;
import com.sankuai.octo.statistic.domain.Instance;
import com.sankuai.octo.statistic.domain.InstanceKey;
import com.sankuai.octo.statistic.metric.MetricManager;
import com.sankuai.octo.statistic.model.StatGroup;
import com.sankuai.octo.statistic.model.StatRange;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

*/
/**
 * Created by wujinwu on 15/12/28.
 *//*

public class InstanceCollector implements IJMonitorCollector {
    @Override
    public void doCollect(IJMonitorStore store) {
        ConcurrentHashMap<InstanceKey, Instance> map = MetricManager.hourInstanceMap();
        if (!map.isEmpty()) {
            Map<StatRange, Map<StatGroup, Integer>> countMap = new HashMap<>();
            Map<StatRange, Map<StatGroup, Long>> sizeMap = new HashMap<>();
            Iterator<Map.Entry<InstanceKey, Instance>> itr = map.entrySet().iterator();
            long timeMillis = System.currentTimeMillis();
            while (itr.hasNext()) {
                Instance instance = itr.next().getValue();
                addCount(countMap, instance);
                addSize(sizeMap, instance);
            }

            long totalSize = 0L;
            int totalCount = 0;

            for (StatRange range : StatRange.values()) {
                Map<StatGroup, Long> sizeGroupMap = sizeMap.get(range);
                Map<StatGroup, Integer> countGroupMap = countMap.get(range);
                long rangeSize = 0L;
                int rangeCount = 0;
                for (StatGroup group : StatGroup.values()) {
                    Long size = 0L;
                    if (sizeGroupMap != null && sizeGroupMap.containsKey(group)) {
                        size = sizeGroupMap.get(group);
                    }
                    Integer count = 0;
                    if (countGroupMap != null && countGroupMap.containsKey(group)) {
                        count = countGroupMap.get(group);
                    }
                    String sizeKey = "stat.instance." + range + "." + group + ".size";
                    String countKey = "stat.instance." + range + "." + group + ".count";
                    store.store(this, sizeKey, size, timeMillis);
                    store.store(this, countKey, count, timeMillis);

                    //  计算 size / count的比例,得出平均每个Instance的size
                    Double avgSize = 0D;
                    if (count != 0) {
                        avgSize = size.doubleValue() / count;
                    }
                    String avgSizeKey = "stat.instance." + range + "." + group + ".avgSize";
                    store.store(this, avgSizeKey, avgSize, timeMillis);

                    //  将 size 与 count叠加
                    rangeSize += size;
                    rangeCount += count;
                }

                String sizeKey = "stat.instance." + range + ".groupAll.size";
                String countKey = "stat.instance." + range + ".groupAll.count";
                store.store(this, sizeKey, rangeSize, timeMillis);
                store.store(this, countKey, rangeCount, timeMillis);

                //  计算 size / count的比例,得出平均每个Instance的size
                Double avgSize = 0D;
                if (rangeCount != 0) {
                    avgSize = (double) rangeSize / rangeCount;
                }
                String avgSizeKey = "stat.instance." + range + ".groupAll.avgSize";
                store.store(this, avgSizeKey, avgSize, timeMillis);


                //  将 size 与 count叠加
                totalSize += rangeSize;
                totalCount += rangeCount;
            }

            String sizeKey = "stat.instance.size";
            String countKey = "stat.instance.count";
            store.store(this, sizeKey, totalSize, timeMillis);
            store.store(this, countKey, totalCount, timeMillis);

            //  计算 size / count的比例,得出平均每个Instance的size
            Double avgSize = 0D;
            if (totalCount != 0) {
                avgSize = (double) totalSize / totalCount;
            }
            String avgSizeKey = "stat.instance.avgSize";
            store.store(this, avgSizeKey, avgSize, timeMillis);

        }
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

    private void addCount(Map<StatRange, Map<StatGroup, Integer>> countMap, Instance instance) {
        if (countMap.containsKey(instance.key().range())) {
            Map<StatGroup, Integer> groupMap = countMap.get(instance.key().range());
            if (groupMap.containsKey(instance.key().group())) {
                Integer count = groupMap.get(instance.key().group());
                groupMap.put(instance.key().group(), count + 1);
            } else {
                groupMap.put(instance.key().group(), 1);
            }
        } else {
            Map<StatGroup, Integer> groupMap = new HashMap<>();
            groupMap.put(instance.key().group(), 1);
            countMap.put(instance.key().range(), groupMap);
        }
    }


    private void addSize(Map<StatRange, Map<StatGroup, Long>> sizeMap, Instance instance) {
        if (sizeMap.containsKey(instance.key().range())) {
            Map<StatGroup, Long> groupMap = sizeMap.get(instance.key().range());
            if (groupMap.containsKey(instance.key().group())) {
                Long size = groupMap.get(instance.key().group());
                groupMap.put(instance.key().group(), size + instance.size());
            } else {
                groupMap.put(instance.key().group(), (long) instance.size());
            }
        } else {
            Map<StatGroup, Long> groupMap = new HashMap<>();
            groupMap.put(instance.key().group(), (long) instance.size());
            sizeMap.put(instance.key().range(), groupMap);
        }
    }
}
*/
