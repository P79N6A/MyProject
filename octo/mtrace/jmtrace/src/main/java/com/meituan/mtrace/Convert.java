package com.meituan.mtrace;

import com.meituan.mtrace.thrift.model.StatusCode;
import com.meituan.mtrace.thrift.model.ThriftSpan;

import java.util.LinkedList;
import java.util.List;

public class Convert {

    /**
     * Span convert to thrift type
     */
    public static com.meituan.mtrace.thrift.model.ThriftSpan spanToThrift(final Span span) {

        ThriftSpan thriftSpan = new ThriftSpan();
        try {
            thriftSpan.setTraceId(Long.valueOf(span.getTraceId()));
        } catch (NumberFormatException e) {
            return null;
        }
        thriftSpan.setSpanId(span.getSpanId());
        thriftSpan.setSpanName(span.getSpanName());
        thriftSpan.setStart(span.getStart());
        thriftSpan.setDuration(span.getCost());
        thriftSpan.setClientSide(span.getType() == Span.SIDE.CLIENT);
        thriftSpan.setLocal(endpointToThrift(span.getLocal()));
        thriftSpan.setRemote(endpointToThrift(span.getRemote()));
        thriftSpan.setInfraName(span.getInfraName());
        thriftSpan.setInfraVersion(span.getVersion());
        if (span.getPackageSize() != 0) {
            thriftSpan.setPackageSize(span.getPackageSize());
        }
        StatusCode status = StatusCode.SUCCESS;
        switch (span.getStatusCode()) {
            case SUCCESS: status = StatusCode.SUCCESS; break;
            case EXCEPTION: status = StatusCode.EXCEPTION; break;
            case TIMEOUT: status = StatusCode.TIMEOUT; break;
            case DROP: status = StatusCode.DROP; break;
            default: status = StatusCode.SUCCESS;
        }
        thriftSpan.setStatus(status);

        List<Annotation> annotations = span.getAnnotations();
        if (annotations != null) {
            thriftSpan.setAnnotations(new LinkedList<com.meituan.mtrace.thrift.model.Annotation>());
            for (Annotation annotation : annotations) {
                thriftSpan.addToAnnotations(annotationToThrift(annotation));
            }
        }

        List<KVAnnotation> kvAnnotations = span.getKvAnnotations();
        if (kvAnnotations != null) {
            thriftSpan.setKvAnnotations(new LinkedList<com.meituan.mtrace.thrift.model.KVAnnotation>());
            for (KVAnnotation kvAnnotation : kvAnnotations) {
                thriftSpan.addToKvAnnotations(kvAnnotationToThrift(kvAnnotation));
            }
        }
        int mask = thriftSpan.getMask();
        if (span.isAsync()) {
            mask = returnAsyncMask(mask);
        }

        if (span.isDebug()) {
            mask = returnDebugMask(mask);
        }
        thriftSpan.setMask(mask);
        return thriftSpan;
    }

    /**
     * Annotation convert to thrift type
     */
    public static com.meituan.mtrace.thrift.model.Annotation annotationToThrift(final Annotation annotation) {
        com.meituan.mtrace.thrift.model.Annotation tAnnotation = new com.meituan.mtrace.thrift.model.Annotation();
        tAnnotation.setValue(annotation.getValue());
        tAnnotation.setTimestamp(annotation.getTimestamp());
        tAnnotation.setDuration(annotation.getDuration());
        return tAnnotation;
    }

    /**
     * KVAnnotation convert to thrift
     */
    public static com.meituan.mtrace.thrift.model.KVAnnotation kvAnnotationToThrift(final KVAnnotation kvAnnotation) {
        com.meituan.mtrace.thrift.model.KVAnnotation tKVAnnotation = new com.meituan.mtrace.thrift.model.KVAnnotation();
        tKVAnnotation.setKey(kvAnnotation.getKey());
        tKVAnnotation.setValue(kvAnnotation.getValue());
        return tKVAnnotation;
    }

    /**
     * Endpoint convert to thrift
     */
    public static com.meituan.mtrace.thrift.model.Endpoint endpointToThrift(final Endpoint endpoint) {
        com.meituan.mtrace.thrift.model.Endpoint tEndpoint = new com.meituan.mtrace.thrift.model.Endpoint(0, (short) 0, "");
        tEndpoint.setAppKey(endpoint.getAppkey());
        tEndpoint.setIp(ipToInt(endpoint.getHost()));
        tEndpoint.setPort((short) endpoint.getPort());
        return tEndpoint;
    }

    /**
     * string ip to int ip
     * @param ip string ip
     * @return int ip
     */
    public static int ipToInt(String ip) {
        if (ip == null) {
            return 0;
        }
        int i = ip.lastIndexOf("//");
        if (i >= 0) {
            ip = ip.substring(i + 2);
        }
        String[] items = ip.split("\\.");
        try {
            return Integer.valueOf(items[0]) << 24 | Integer.valueOf(items[1]) << 16 | Integer.valueOf(items[2]) << 8 | Integer.valueOf(items[3]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * int ip to string ip
     * @param ip int ip
     * @return string ip
     */
    public static String intToIp(int ip) {
        StringBuilder sb = new StringBuilder();
        //直接右移24位
        sb.append(String.valueOf((ip >>> 24)));
        sb.append(".");
        //将高8位置0，然后右移16位
        sb.append(String.valueOf((ip & 0x00FFFFFF) >>> 16));
        sb.append(".");
        //将高16位置0，然后右移8位
        sb.append(String.valueOf((ip & 0x0000FFFF) >>> 8));
        sb.append(".");
        //将高24位置0
        sb.append(String.valueOf((ip & 0x000000FF)));
        return sb.toString();
    }

    /**
     * mask
     * 0 : debug
     * 1 :
     * 2 : async
     * ...
     */

    public static int returnDebugMask(int mask) {
        return mask | 1;
    }
    public static int returnAsyncMask(int mask) {
        return mask | (1 << 2);
    }
    public static boolean isAsync(int mask) {
        int async = 1 << 2;
        return (mask & async) == async;
    }
    public static boolean isDebug(int mask) {
        int debug = 1;
        return (mask & debug) == debug;
    }

}
