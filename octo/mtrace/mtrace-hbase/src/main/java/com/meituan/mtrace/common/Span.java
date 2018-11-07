package com.meituan.mtrace.common;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zhangzhitong
 * @created 9/24/15
 */
public class Span {
    private long traceId;
    private String spanId;
    private String spanName;
    private Endpoint clientEp;
    private Endpoint serverEp;
    private long start;
    private int duration;
    private boolean clientSide;
    private List<Annotation> annotations;

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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public void setClientSide(boolean clientSide) {
        this.clientSide = clientSide;
    }
    public String toString() {
        return "Span(traceId:" + this.traceId +
                ", spanId:" + this.spanId +
                ", spanName:" + this.spanName +
                ", clientEp:" + this.clientEp +
                ", serverEp:" + this.serverEp +
                ", start:" + this.start +
                ", duration:" + this.duration +
                ", clientSide:" + this.clientSide +
                ", annotations:" + this.annotations +
                ")";

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
    public boolean isRootSpan() {
        return spanId != null && spanId.equals("0");
    }
}
