package com.meituan.mtrace;

import com.meituan.mtrace.collector.ScribeLogCollector;
import com.meituan.mtrace.collector.TraceCollector;

import com.meituan.mtrace.octo.SlowQueryFilter;
public class TraceContext {
    private final static ThreadLocal<Span> CURRENT_SERVER_SPAN = new ThreadLocal<Span>();
    private final static ThreadLocal<Span> CURRENT_CLIENT_SPAN = new ThreadLocal<Span>();
    private final static ThreadLocal<Boolean> DEBUG_FLAG = new ThreadLocal<Boolean>();
    private MtraceConfig config;
    public final SlowQueryFilter slowQueryFilter = new SlowQueryFilter();
    public TraceCollector traceCollector = null;
    public ScribeLogCollector scribeCollector = null;


    public TraceContext() {
        config = MtraceConfig.getInstance();
        this.traceCollector = new TraceCollector();
        if (!config.isUploadSgAgent()) {
            this.traceCollector.setActive(false);
        }
        if (config.isUploadFlume()) {
            this.scribeCollector = new ScribeLogCollector();
        }
    }

    public void setDebugFlag(Boolean debugFlag) {
        DEBUG_FLAG.set(debugFlag);
    }

    public Boolean removeDebugFlag() {
        Boolean debug = DEBUG_FLAG.get();
        DEBUG_FLAG.remove();
        return debug;
    }

    public Span getCurrentServerSpan() {
        return CURRENT_SERVER_SPAN.get();
    }

    public void setCurrentServerSpan(final Span span) {
        if (span == null) {
            CURRENT_SERVER_SPAN.remove();
        } else {
            CURRENT_SERVER_SPAN.set(span);
        }
    }

    public Span getCurrentClientSpan() {
        return CURRENT_CLIENT_SPAN.get();
    }

    public void setCurrentClientSpan(final Span span) {
        CURRENT_CLIENT_SPAN.set(span);
    }
}
