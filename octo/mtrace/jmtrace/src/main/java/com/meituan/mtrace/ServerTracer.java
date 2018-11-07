package com.meituan.mtrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerTracer extends AbstractTracer {
    private final static Logger logger = LoggerFactory.getLogger(ServerTracer.class);

    public final Span getSpan() {
        return TRACE_CONTEXT.getCurrentServerSpan();
    }

    public void clearCurrentSpan() {
        TRACE_CONTEXT.setCurrentServerSpan(null);
    }

    private static ServerTracer instance = new ServerTracer();

    private ServerTracer() {
    }

    public static ServerTracer getInstance() {
        return instance;
    }

    private boolean isTraceHead(final Span span) {
        return span.getSpanId().equals("0");
    }

    public String getAppKey() {
        Span span = TRACE_CONTEXT.getCurrentServerSpan();
        return span != null ? span.getLocalAppKey() : null;
    }

    public String getRemoteAppKey() {
        Span span = TRACE_CONTEXT.getCurrentServerSpan();
        return span != null ? span.getRomoteAppKey() : null;
    }

    public Span record(TraceParam param) {
        String traceId = param.getTraceId();
        String spanId = param.getSpanId();
        // 如果传递了traceId并且是有效的则按照传递的traceId 生成Span,否则重新生成Span
        Span span;
        if (isValidTraceId(traceId)) {
            span = new Span(traceId, spanId, param.getSpanName());
        } else {
            span = new Span(param.getSpanName());
            span.setDebug(TRACE_CONTEXT.traceCollector.isSample());
        }
        span.setLocal(param.getLocalAppKey(), param.getLocalIp(), param.getLocalPort());
        span.setRemote(param.getRemoteAppKey(), param.getRemoteIp(), param.getRemotePort());
        span.setStart(System.currentTimeMillis());
        span.setType(Span.SIDE.SERVER);
        span.setExtend(param.getExtend());
        if (param.isDebug()) {
            span.setDebug(true);
        }
        TRACE_CONTEXT.setCurrentServerSpan(span);
        return span;
    }

    public Span flush() {
        Span span = getSpan();
        if (span != null) {
            span.setEnd(System.currentTimeMillis());
            collect(span);
            clearCurrentSpan();
        }
        return span;
    }

    /* ---------- Deprecated Methods---------- */

    /**
     * @param traceId
     * @param spanId
     * @param spanName
     * @param local
     * @param debug    0普通模式，1测试模式
     */
    @Deprecated
    private void setCurrentTrace(final String traceId, final String spanId, final String spanName, final Endpoint local, Boolean sample, short debug) {
        Validate.notBlank(spanName, "Span name can't be empty or null.");
        // TODO 为统计调用链路做的特殊处理，调用链头节点判断条件多加了一个是否是老的traceId(uuid string)，以后版本中可以去掉判断老traceId的条件
        if (!Validate.isUuidStr(traceId)) {
            Span span = new Span(traceId, spanId, spanName);
            span.setLocal(local);
            span.setSample(sample);
            TRACE_CONTEXT.setCurrentServerSpan(span);
        } else {
            setCurrentTrace(spanName, local, sample, null);
        }
    }

    @Deprecated
    public void setCurrentTrace(final String traceId, final String spanId, final String spanName, final Endpoint local, Boolean sample, Boolean debug) {
        setCurrentTrace(traceId, spanId, spanName, local, sample, (short) ((debug != null && debug) ? 1 : 0));
    }

    @Deprecated
    public void setCurrentTrace(final String traceId, final String spanId, final String spanName, final Endpoint local, Boolean sample) {
        setCurrentTrace(traceId, spanId, spanName, local, sample, (short) 0);
    }

    @Deprecated
    public void setCurrentTrace(final String traceId, final String spanId, final String spanName, final Endpoint local) {
        setCurrentTrace(traceId, spanId, spanName, local, null, (short) 0);
    }

    @Deprecated
    public void setCurrentTrace(final String spanName, final Endpoint local) {
        setCurrentTrace(spanName, local, null, null);
    }

    @Deprecated
    public void setCurrentTrace(final String spanName, final Endpoint local, Boolean sample, Boolean debug) {
        Validate.notBlank(spanName, "Span name can't be empty or null.");
        Span span = new Span(spanName);
        span.setLocal(local);
        span.setSample(sample);
        TRACE_CONTEXT.setCurrentServerSpan(span);
    }

    @Deprecated
    private short getDebugFlag(short debug) {
        if (debug != 1) {
            Boolean debugFlag = TRACE_CONTEXT.removeDebugFlag();
            debug = (short) ((debugFlag != null && debugFlag) ? 1 : 0);
        }
        return debug;
    }

    @Deprecated
    public void setServerReceived(Endpoint remote) {
        final Span span = getSpan();
        if (span != null) {
            span.setServerReceived(remote);
        }
    }

    @Deprecated
    public void setServerSend(int status) {
        final Span span = getSpan();
        if (span == null) {
            return;
        }
        span.setServerSend(status);
        collect(span);
        clearCurrentSpan();
    }

}
