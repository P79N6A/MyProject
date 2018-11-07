package com.meituan.mtrace.common;

import java.util.LinkedList;
import java.util.List;

/**
 * Call 代表一次rpc调用的信息
 * 一次Call 包括client span 和 server span
 *
 * @author zhangzhitong
 * @created 11/12/15
 */

public class Call {
    private long traceId;
    private String spanId;
    private String spanName;
    private Endpoint clientEp;
    private Endpoint serverEp;
    private long clientStart;
    private long serverStart;
    private int clientDuration;
    private int serverDuration;
    private List<Annotation> annotations = new LinkedList<Annotation>();

    public Call(Span span) {
        this.traceId = span.getTraceId();
        this.spanId = span.getSpanId();
        this.spanName = span.getSpanName();
        this.clientEp = span.getClientEp();
        this.serverEp = span.getServerEp();
        this.clientStart = span.getStart();
        this.clientDuration = span.getDuration();
        this.serverStart = span.getStart();
        this.serverDuration = span.getDuration();
        this.annotations = new LinkedList<Annotation>();
        if (span.getAnnotations() != null && span.getAnnotations().size() > 0) {
            this.annotations.addAll(span.getAnnotations());
        }

    }

    public long getTraceId() {
        return traceId;
    }

    public void setTraceId(long traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    public Endpoint getClientEp() {
        return clientEp;
    }

    public void setClientEp(Endpoint clientEp) {
        this.clientEp = clientEp;
    }

    public Endpoint getServerEp() {
        return serverEp;
    }

    public void setServerEp(Endpoint serverEp) {
        this.serverEp = serverEp;
    }

    public long getClientStart() {
        return clientStart;
    }

    public void setClientStart(long clientStart) {
        this.clientStart = clientStart;
    }

    public long getServerStart() {
        return serverStart;
    }

    public void setServerStart(long serverStart) {
        this.serverStart = serverStart;
    }

    public int getClientDuration() {
        return clientDuration;
    }

    public void setClientDuration(int clientDuration) {
        this.clientDuration = clientDuration;
    }

    public int getServerDuration() {
        return serverDuration;
    }

    public void setServerDuration(int serverDuration) {
        this.serverDuration = serverDuration;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    /**
     * client span merge server span or server span merge client span
     * 含有时间校正逻辑, 约束: client span 开始时间要小于server span 开始时间, client span 结束时间要大于server span 结束时间
     *
     * @param span client span or server span,
     */
    public void mergeSpan(Span span) {
        if (span.isClientSide()) {
            if (this.spanName == null || this.spanName.isEmpty())
                this.spanName = span.getSpanName();
            this.clientEp = span.getClientEp();
            if (span.getStart() <= this.serverStart) {
                if (span.getStart() + span.getDuration() < this.serverStart + this.serverDuration) {
                    this.clientStart = this.serverStart + this.serverDuration - span.getDuration();
                } else {
                    this.clientStart = span.getStart();
                }

            }
            this.clientDuration = span.getDuration();
        } else {
            this.serverEp = span.getServerEp();
            if (span.getStart() < this.clientStart) {
                this.clientStart = span.getStart();
            } else if (span.getStart() + span.getDuration() > this.clientStart + this.clientDuration) {
                this.clientStart = span.getStart() + span.getDuration() - this.clientDuration;
            }
            this.serverStart = span.getStart();
            this.serverDuration = span.getDuration();
            if (span.getSpanName() != null && !span.getSpanName().isEmpty())
                this.spanName = span.getSpanName();
        }
        if (span.getAnnotations() != null && span.getAnnotations().size() > 0)
            this.annotations.addAll(span.getAnnotations());
    }

    public int getDepth() {
        return getDepth(this.spanId);
    }

    public static int getDepth(String spanId) {
        if (spanId == null || spanId.isEmpty()) {
            return 1;
        }
        int depth = 1;
        for (char c : spanId.toCharArray()) {
            if (c == '.') {
                ++depth;
            }
        }
        return depth;

    }

    public String getParentSpanId() {
        return getParentSpanId(this.spanId);
    }

    public static String getParentSpanId(String spanId) {
        if (spanId == null || spanId.equals("0")) {
            return null;
        } else {
            return spanId.substring(0, spanId.lastIndexOf('.'));
        }
    }

    public boolean isRoot() {
        return this.spanId != null && this.spanId.equals("0");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Call(");
        sb.append("traceId:");
        sb.append(traceId);
        sb.append(",spanId:");
        sb.append(spanId);
        sb.append(",spanName");
        sb.append(spanName);
        sb.append(",clientEp");
        sb.append(clientEp.toString());
        sb.append(",serverEp");
        sb.append(serverEp.toString());
        sb.append(",clientStart");
        sb.append(clientStart);
        sb.append(",serverStart");
        sb.append(serverStart);
        sb.append(",clientDuration");
        sb.append(clientDuration);
        sb.append(",serverDuration");
        sb.append(serverDuration);
        sb.append(",annotations");
        sb.append(annotations);
        return sb.toString();
    }
}
