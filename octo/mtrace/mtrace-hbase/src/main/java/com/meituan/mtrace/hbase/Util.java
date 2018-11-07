package com.meituan.mtrace.hbase;

import com.meituan.mtrace.common.Annotation;
import com.meituan.mtrace.common.Constants;
import com.meituan.mtrace.common.Endpoint;
import com.meituan.mtrace.common.Span;
import com.meituan.mtrace.thriftjava.ThriftSpan;

import java.util.LinkedList;
import java.util.List;

/**
 * @author zhangzhitong
 * @created 9/24/15
 */
public class Util {
    public static Span thriftToSpan(ThriftSpan tSpan) {
        if (tSpan == null) {
            return null;
        }
        Span span = new Span();
        span.setTraceId(tSpan.getTraceId());
        span.setSpanId(tSpan.getSpanId());
        span.setSpanName(tSpan.getSpanName());
        span.setStart(tSpan.getStart());
        span.setDuration(tSpan.getDuration());
        if (tSpan.isClientSide()) {
            span.setClientEp(Util.thriftToEndpoint(tSpan.getLocal()));
            span.setServerEp(Util.thriftToEndpoint(tSpan.getRemote()));
        } else {
            span.setClientEp(Util.thriftToEndpoint(tSpan.getRemote()));
            span.setServerEp(Util.thriftToEndpoint(tSpan.getLocal()));
        }
        span.setClientSide(tSpan.isClientSide());
        List<Annotation> annotations = new LinkedList<Annotation>();
        //
        Annotation anno1 = new Annotation();
        if (span.isClientSide()) {
            anno1.setValue(Constants.CLIENT_SEND);
            anno1.setEndpoint(span.getClientEp());
        } else {
            anno1.setValue(Constants.SERVER_RECV);
            anno1.setEndpoint(span.getServerEp());
        }
        anno1.setTimestamp(span.getStart());
        annotations.add(0, anno1);

        if (tSpan.isSetAnnotations() && !tSpan.getAnnotations().isEmpty()) {
            for (com.meituan.mtrace.thriftjava.Annotation tAnno : tSpan.getAnnotations()) {
                annotations.add(thriftToAnnotation(tAnno));
            }
        }

        //
        Annotation anno2 = new Annotation();
        if (span.isClientSide()) {
            anno2.setValue(Constants.CLIENT_RECV);
        } else {
            anno2.setValue(Constants.SERVER_SEND);
        }
        anno2.setTimestamp(span.getStart() + span.getDuration());
        annotations.add(anno2);
        span.setAnnotations(annotations);
        return span;
    }

    public static Endpoint thriftToEndpoint(com.meituan.mtrace.thriftjava.Endpoint tEp) {
        return tEp == null ? null : new Endpoint(tEp.getAppKey(), tEp.getIp(), tEp.getPort());
    }

    public static Annotation thriftToAnnotation(com.meituan.mtrace.thriftjava.Annotation tAnno) {
        if (tAnno == null) {
            return null;
        }
        Annotation anno = new Annotation();
        anno.setValue(tAnno.getValue());
        anno.setDuration(tAnno.getDuration());
        anno.setTimestamp(tAnno.getTimestamp());
        if (tAnno.isSetEndpoint()) {
            anno.setEndpoint(Util.thriftToEndpoint(tAnno.getEndpoint()));
        }
        return anno;
    }
}
