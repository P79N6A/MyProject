package com.meituan.mtrace;

import com.meituan.mtrace.octo.SlowQueryFilter;

public abstract class AbstractTracer implements ITracer {
    protected final static TraceContext TRACE_CONTEXT = new TraceContext();

    public String getTraceId() {
        Span span = TRACE_CONTEXT.getCurrentServerSpan();
        return span != null ? span.getTraceId() : null;
    }

    public abstract Span getSpan();

    public void addAnnotation(String value) {
        addAnnotation(value, 0);
    }

    public void addAnnotation(String value, int duration) {
        if (value != null) {
            Annotation annotation = new Annotation(value, System.currentTimeMillis(), duration);
            Span span = getSpan();
            if (span != null)
                getSpan().addAnnotation(annotation);
        }
    }

    public void addAnnotation(String key, String value) {
        if (key != null && value != null) {
            KVAnnotation kvAnnotation = new KVAnnotation(key, value);
            Span span = getSpan();
            if (span != null)
                span.addKvAnnotation(kvAnnotation);
        }
    }

    public static SlowQueryFilter getSlowQueryFilter() {
        return TRACE_CONTEXT.slowQueryFilter;
    }

    protected boolean isValidTraceId(String traceId) {
        return traceId != null && !traceId.isEmpty() && !Validate.isUuidStr(traceId);
    }

    protected void collect(final Span span) {
        if (TRACE_CONTEXT.traceCollector != null) {
            TRACE_CONTEXT.traceCollector.collect(span);
        }
        if (TRACE_CONTEXT.scribeCollector != null) {
            TRACE_CONTEXT.scribeCollector.collect(span);
        }
    }
}
