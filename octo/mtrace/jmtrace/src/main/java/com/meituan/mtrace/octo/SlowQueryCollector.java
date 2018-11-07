package com.meituan.mtrace.octo;

import com.meituan.mtrace.Span;
import com.meituan.mtrace.Validate;
import com.sankuai.sgagent.thrift.model.TraceThresholdLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SlowQueryCollector {
    private static final Logger LOG = LoggerFactory.getLogger(SlowQueryCollector.class);
    private final int period = 10000;
    private volatile long lastPeriod;
    private AtomicInteger periodTotalCount = new AtomicInteger(0);
    private AtomicLong periodTotalCost = new AtomicLong(0);
    private int threshold = 1000; // 单位: ms

    public SlowQueryCollector(int threshold) {
        this.threshold = threshold;
    }

    public void doFilter(Span span) {
        int cost = span.getCost();
        int isFilter = (cost > threshold ? 1 : 0);
        long currentPeriod = System.currentTimeMillis() / period;
        if (currentPeriod == lastPeriod) {
            if (isFilter == 1) {
                // 同周期内仅更新内存统计值
                periodTotalCount.getAndAdd(isFilter);
                periodTotalCost.getAndAdd(cost);
            }
        } else {
            lastPeriod = currentPeriod;
            int totalCount = periodTotalCount.get();
            long totalCost = periodTotalCost.get();
            // 采集之前周期的的统计值
            if (totalCount > 0) {
                collect(span, totalCount, totalCost);
            }
            // 重置统计值
            periodTotalCount.set(isFilter);
            periodTotalCost.set(isFilter == 1 ? cost : 0);
        }
    }

    private void collect(Span span, int count, long cost) {
        // TODO：这里先暂时基于Span构造，目前只有appkey（local/remote）、spanname、count、cost是准确的，其它字段无用，可优化
        TraceThresholdLog log = toThresholdLog(span);
        log.setCount(count);
        // 先使用平均耗时
        log.setCost((int) (count != 0 ? 1.0 * cost / count : 1.0 * cost));
        if (LOG.isDebugEnabled()) {
            LOG.info("collect TraceThresholdLog " + count + "," + cost + "," + log);
        }
        AsyncOctoCollector.getInstance().collectSlowQuery(log);
    }

    private TraceThresholdLog toThresholdLog(Span span) {
        TraceThresholdLog log = new TraceThresholdLog();
        log.setTraceId(span.getTraceId());
        log.setSpanId(span.getSpanId());
        log.setSpanName(span.getSpanName());
        if (span.getLocal() != null) {
            log.setLocalAppKey(span.getLocal().getAppkey());
            log.setLocalHost(Validate.isBlank(span.getLocal().getHost()) ? OctoCollector.appIp : span.getLocal().getHost());
            log.setLocalPort(span.getLocal().getPort());
        }
        if (span.getRemote() != null) {
            log.setRemoteAppKey(span.getRemote().getAppkey());
            log.setRemoteHost(span.getRemote().getHost());
            log.setRemotePort(span.getRemote().getPort());
        }
        log.setStart(span.getStart());
        log.setCost(span.getCost());
        log.setType(span.getType().getValue());
        log.setStatus(span.getStatus());
        log.setDebug(span.isDebug() ? 1 : 0);
        log.setExtend(String.valueOf(span.getExtend()));
        return log;
    }
}
