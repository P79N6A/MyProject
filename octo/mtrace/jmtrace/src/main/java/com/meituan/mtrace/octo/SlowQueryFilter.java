package com.meituan.mtrace.octo;

import com.meituan.mtrace.Span;
import com.meituan.mtrace.sample.ConcurrentLRUHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SlowQueryFilter {
    private static final Logger LOG = LoggerFactory.getLogger(SlowQueryFilter.class);
    private Map<String, SlowQueryCollector> collectors = new ConcurrentLRUHashMap<String, SlowQueryCollector>(10000);

    public void setThreshold(String appkey, String spanname, int threshold) {
        collectors.put(appkey + "|" + spanname, new SlowQueryCollector(threshold));
    }

    public void setThreshold(String appkey, int threshold) {
        collectors.put(appkey + "|*", new SlowQueryCollector(threshold));
    }

    public void filter(Span span) {
        if (span == null || collectors.isEmpty()) {
            return;
        }
        try {
            // 仅从appkey+spanname维度，暂不支持host等其它维度
            String prefix = getKeyPrefix(span);
            SlowQueryCollector collector = collectors.get(prefix + span.getSpanName());
            if (collector != null) {
                collector.doFilter(span);
            } else {
                SlowQueryCollector defaultCollector = collectors.get(prefix + "*");
                if (defaultCollector != null) {
                    defaultCollector.doFilter(span);
                }
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("slow query filter exception " + span, e);
            }
        }
    }

    private String getKeyPrefix(Span span) {
        if (span.getType().getValue() == 1 && span.getLocal() != null) {
            return span.getLocal().getAppkey() + "|";
        } else if (span.getType().getValue() == 0 && span.getRemote() != null) {
            return span.getRemote().getAppkey() + "|";
        } else {
            return "unknownService|";
        }
    }
}
