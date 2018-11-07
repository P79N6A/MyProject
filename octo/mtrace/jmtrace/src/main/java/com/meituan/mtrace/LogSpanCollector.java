package com.meituan.mtrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogSpanCollector {
    private final static Logger LOG = LoggerFactory.getLogger(LogSpanCollector.class);

    public static void collect(final Span span) {
        Validate.notNull(span);
        LOG.info(span.toString());
    }

    private static Boolean mtraceCollectorByLog;
    private static Boolean mtraceCollectorByFlume;
    private static Boolean enableOcto;

    public static void setMtraceCollectorByLog(Boolean mtraceCollectorByLog) {
        LogSpanCollector.mtraceCollectorByLog = mtraceCollectorByLog;
    }

    public static void setMtraceCollectorByFlume(Boolean mtraceCollectorByFlume) {
        LogSpanCollector.mtraceCollectorByFlume = mtraceCollectorByFlume;
    }

    public static void setEnableOcto(Boolean enableOcto) {
        LogSpanCollector.enableOcto = enableOcto;
    }

    public static boolean isMtraceCollectorByLog() {
        if (mtraceCollectorByLog == null) {
            String value = System.getProperty("mtrace.collector");
            mtraceCollectorByLog = (value != null && "log".equalsIgnoreCase(value));
        }
        return mtraceCollectorByLog;
    }

    public static boolean isMtraceCollectorByFlume() {
        if (mtraceCollectorByFlume == null) {
            String value = System.getProperty("mtrace.collector");
            mtraceCollectorByFlume = (value == null || "flume".equalsIgnoreCase(value));
        }
        return mtraceCollectorByFlume;
    }

    public static boolean isEnableOcto() {
        if (enableOcto == null) {
            String value = System.getProperty("octo.collector");
            enableOcto = (value != null && "true".equalsIgnoreCase(value));
        }
        return enableOcto;
    }
}
