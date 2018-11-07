package com.meituan.service.mobile.mtthrift.util;

import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wujinwu on 16/7/25.
 */
public class LoadInfoUtil {
    private static final Logger LOG = LoggerFactory.getLogger(LoadInfoUtil.class);
    private static final String[] CpuLoadName = new String[]{"ProcessCpuLoad"};
    private static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private static ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor(new MTDefaultThreadFactory("LoadInfoUtil"));

    private static volatile long lastOldGcCount = 0;

    //  本周期内old gc次数
    private static volatile int currentOldGcCount;

    private static volatile double avgLoad;

    private static final int ONE_MINUTE_SECONDS = 60;

    private static ConcurrentMap<String, AtomicInteger> invocationMap = new ConcurrentHashMap<String, AtomicInteger>();

    static {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                doTask();
            }
        };
        es.scheduleAtFixedRate(r, 10, 10, TimeUnit.SECONDS);
        Runnable qpsResetTask = new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, AtomicInteger> entry : invocationMap.entrySet()) {
                    entry.getValue().set(0);
                }
            }
        };
        es.scheduleAtFixedRate(qpsResetTask, 1, 1, TimeUnit.MINUTES);

    }

    private static void doTask() {
        updateOldGcCount();
        updateProcessCpuLoad();
    }

    private static void updateOldGcCount() {
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

        GarbageCollectorMXBean majorGC = gcMXBeans.get(1);
        long oldGcTotalCount = majorGC.getCollectionCount();
        currentOldGcCount = (int) (oldGcTotalCount - lastOldGcCount);
        lastOldGcCount = oldGcTotalCount;

    }

    private static void updateProcessCpuLoad() {
        avgLoad = getProcessCpuLoad();
    }


    private static double getProcessCpuLoad() {
        try {
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, CpuLoadName);
            if (list == null || list.isEmpty()) {
                return 0;
            }
            Attribute att = (Attribute) list.get(0);
            Double value = (Double) att.getValue();
            return value < 0 ? 0 : value;
        } catch (Exception e) {
            LOG.debug("getProcessCpuLoad failed...", e);
            return 0;
        }
    }

    public static int getOldGcCount() {
        return currentOldGcCount;
    }

    public static double getAvgLoad() {
        return avgLoad;
    }

    /**
     * @param spanName interface.method
     */
    public static void incrSpanInvocationCount(String spanName) {
        AtomicInteger count = invocationMap.get(spanName);
        if (count == null) {
            AtomicInteger c = new AtomicInteger();
            invocationMap.putIfAbsent(spanName, c);
            count = invocationMap.get(spanName);
        }

        count.incrementAndGet();
    }

    public static Map<String, Double> getQpsMap() {
        if (invocationMap.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<String, Double> map = new HashMap<String, Double>();

            for (Map.Entry<String, AtomicInteger> entry : invocationMap.entrySet()) {
                map.put(entry.getKey(), (double) entry.getValue().get() / ONE_MINUTE_SECONDS);
            }
            return map;
        }

    }
}
