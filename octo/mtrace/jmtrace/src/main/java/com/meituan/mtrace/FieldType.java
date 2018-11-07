package com.meituan.mtrace;

public enum FieldType {
    TraceId("M-TraceId"),
    SpanId("M-SpanId"),
    Appkey("M-Appkey"),
    SpanName("M-SpanName"),
    Sample("M-Sample"),
    Debug("M-Debug"),
    Host("M-Host"),
    Port("M-Port");

    private final String name;

    FieldType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
