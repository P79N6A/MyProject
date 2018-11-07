package com.meituan.service.mobile.mtthrift.mtrace;

import com.meituan.mtrace.Span;
import com.meituan.mtrace.Tracer;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 16-3-10
 * Time: 上午11:40
 */
public class MtraceUtils {

    public static void serverMark(Tracer.STATUS status) {
        Span span = Tracer.getServerTracer().getSpan();
        if (span != null)
            span.setStatus(status);
    }

    public static void clientMark(Tracer.STATUS status) {
        Span span = Tracer.getClientTracer().getSpan();
        if (span != null) {
            span.setStatus(status);
            Tracer.clientRecv();
        }
    }
}
