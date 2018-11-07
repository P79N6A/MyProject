package com.meituan.mtrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTracer extends AbstractTracer {
    private final static Logger logger = LoggerFactory.getLogger(ClientTracer.class);
    private static ClientTracer instance = new ClientTracer();

    private ClientTracer() {
    }

    public static ClientTracer getInstance() {
        return instance;
    }

    public final Span getSpan() {
        return TRACE_CONTEXT.getCurrentClientSpan();
    }

    public Span record(TraceParam param) {
        Span span = recordAsync(param);
        TRACE_CONTEXT.setCurrentClientSpan(span);
        return span;
    }

    public Span recordAsync(TraceParam param) {
        final Span currentServerSpan = TRACE_CONTEXT.getCurrentServerSpan();
        final Span span;
        // TODO 为统计调用链路做的特殊处理，头节点判断条件为上级传来老的traceId(uuid string)和上级没有Span信息，以后版本中可以去掉判断老traceId的条件
        if (currentServerSpan == null || Validate.isUuidStr(currentServerSpan.getTraceId())) {
            span = new Span(param.getSpanName());
        } else {
            span = currentServerSpan.genNextSpan(param.getSpanName());
            span.setLocal(currentServerSpan.getLocalAppKey(), currentServerSpan.getLocalHost(), currentServerSpan.getLocalPort());
            if (currentServerSpan.isDebug()) {
                span.setDebug(true);
            }
        }
        span.setStart(System.currentTimeMillis());
        if (Validate.notEmpty(param.getLocalAppKey())) {
            span.getLocal().setAppkey(param.getLocalAppKey());
        }
        if (Validate.notEmpty(param.getLocalAppKey())) {
            span.getLocal().setHost(param.getLocalIp());
        }
        if (param.isDebug()) {
            span.setDebug(true);
        }

        span.setRemote(param.getRemoteAppKey(), param.getRemoteIp(), param.getRemotePort());
        span.setType(Span.SIDE.CLIENT);
        span.setInfraName(param.getInfraName());
        span.setVersion(param.getVersion());
        span.setPackageSize(param.getPackageSize());
        span.setExtend(param.getExtend());
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

    public Span flushAsync(final Span span) {
        if (span != null) {
            span.setEnd(System.currentTimeMillis());
            collect(span);
        }
        return span;
    }

    public void clearCurrentSpan() {
        TRACE_CONTEXT.setCurrentClientSpan(null);
    }

    /* ---------- Deprecated Methods---------- */

    @Deprecated
    public void setClientSent(Endpoint remote) {
        final Span span = getSpan();
        if (span != null) {
            span.setClientSent(remote);
        }
    }

    @Deprecated
    public void setClientReceived(int status) {
        final Span span = getSpan();
        setAsyncClientReceived(span, status);
        clearCurrentSpan();
    }

    /**
     * 用于异步跟踪，异步时span无法从threadLocal中取得
     *
     * @param span
     * @param status
     */
    @Deprecated
    public void setAsyncClientReceived(Span span, int status) {
        if (span == null) {
            return;
        }
        span.setClientReceived(status);
        collect(span);
    }


    @Deprecated
    public void startNewSpan(final String spanName, final Endpoint local) {
        final Span currentServerSpan = TRACE_CONTEXT.getCurrentServerSpan();
        final Span span;
        // TODO 为统计调用链路做的特殊处理，头节点判断条件为上级传来老的traceId(uuid string)和上级没有Span信息，以后版本中可以去掉判断老traceId的条件
        if (currentServerSpan == null || Validate.isUuidStr(currentServerSpan.getTraceId())) {
            span = new Span(spanName);
            span.setLocal(local);
        } else {
            span = currentServerSpan.genNextSpan(spanName);
            span.setLocal(currentServerSpan.getLocal());
        }
        TRACE_CONTEXT.setCurrentClientSpan(span);
    }

}
