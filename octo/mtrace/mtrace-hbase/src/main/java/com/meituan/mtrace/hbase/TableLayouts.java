package com.meituan.mtrace.hbase;

import com.meituan.mtrace.thriftjava.*;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * storageTableName 存储基本数据的表
 * durationTableName 存储简要信息的表名
 * idxServiceSpanNameTableName Service + SpanName 维度的表名
 *
 * @author zhangzhitong
 * @created 9/15/15
 */
public class TableLayouts {
    public final static String ClientSide = "C";
    public final static String ServerSide = "S";

    private final static String storageTableName = "mtrace.traces";
    private final static String storageStage = "mtrace.stage.traces";
    private final static String storageTest = "mtrace.test.traces";
    public final static byte[] storageFamily = Bytes.toBytes("T");

    /*
    public static String durationTableName = "mtrace.duration";
    public final static byte[] durationDurationFamily = Bytes.toBytes("D");
    public final static byte[] durationStartTimeFamily = Bytes.toBytes("S");
    */

    private final static String idxServiceNameTableName = "mtrace.idxServiceName";
    private final static String idxServiceNameStage = "mtrace.stage.idxServiceName";
    private final static String idxServiceNameTest = "mtrace.test.idxServiceName";
    public final static byte[] idxServiceNameFamily = Bytes.toBytes("S");

    private final static String idxServiceSpanNameTableName = "mtrace.idxServiceSpanName";
    private final static String idxServiceSpanNameStage = "mtrace.stage.idxServiceSpanName";
    private final static String idxServiceSpanNameTest = "mtrace.test.idxServiceSpanName";
    public final static byte[] idxServiceSpanNameFamily = Bytes.toBytes("S");

    public static String getStorageTable(Env env) {
        if (env == null || env.equals(Env.Prod)) {
            return storageTableName;
        } else if (env.equals(Env.Stage)) {
            return storageStage;
        } else if (env.equals(Env.Test)) {
            return storageTest;
        }
        return storageTableName;
    }

    public static String getIdxServiceTable(Env env) {
        if (env.equals(Env.Prod)) {
            return idxServiceNameTableName;
        } else if (env.equals(Env.Stage)) {
            return idxServiceNameStage;
        } else if (env.equals(Env.Test)) {
            return idxServiceNameTest;
        }
        return idxServiceNameTableName;
    }

    public static String getIdxServiceSpanTable(Env env) {
        if (env.equals(Env.Prod)) {
            return idxServiceSpanNameTableName;
        } else if (env.equals(Env.Stage)) {
            return idxServiceSpanNameStage;
        } else if (env.equals(Env.Test)) {
            return idxServiceSpanNameTest;
        }
        return idxServiceSpanNameTableName;
    }

    public static byte[] getRowKeyOfStorage(ThriftSpan thriftSpan) {
        return getRowKeyOfStorage(thriftSpan.getTraceId());
    }

    public static byte[] getRowKeyOfStorage(long traceId) {
        return Bytes.toBytes(traceId);
    }

    public static byte[] getQualOfStorage(ThriftSpan thriftSpan) {
        return Bytes.toBytes(thriftSpan.getSpanId() + "-" + (thriftSpan.isClientSide() ? ClientSide : ServerSide));
    }

    public static byte[] getRowKeyOfDuration(ThriftSpan thriftSpan) {
        return Bytes.toBytes(thriftSpan.getTraceId());
    }

    public static byte[] getQualOfDuration(ThriftSpan thriftSpan) {
        return Bytes.toBytes(thriftSpan.getSpanId());
    }

    public static byte[] getRowKeyOfServiceName(ThriftSpan thriftSpan) {
        Endpoint ep = thriftSpan.isClientSide() ? thriftSpan.getRemote() : thriftSpan.getLocal();
        if (ep.getAppKey() != null && !ep.getAppKey().isEmpty()) {
            return getRowKeyOfServiceName(ep.getAppKey(), thriftSpan.getStart());
        }
        return getRowKeyOfServiceName("none", thriftSpan.getStart());
    }

    public static byte[] getRowKeyOfServiceSpanName(ThriftSpan thriftSpan) {
        Endpoint ep = thriftSpan.isClientSide() ? thriftSpan.getRemote() : thriftSpan.getLocal();
        if (ep.getAppKey() != null && !ep.getAppKey().isEmpty()) {
            return getRowKeyOfServiceSpanName(ep.getAppKey(), thriftSpan.getSpanName(), thriftSpan.getStart());
        }
        return getRowKeyOfServiceSpanName("none", thriftSpan.getSpanName(), thriftSpan.getStart());
    }

    public static byte[] getRowKeyOfServiceSpanName(String serviceName, String spanName, long ts) {
        return Bytes.toBytes(serviceName + "-" + spanName + "-" + String.valueOf((Long.MAX_VALUE - ts) / 5000));
    }

    public static byte[] getRowKeyOfServiceName(String serviceName, long ts) {
        return Bytes.toBytes(serviceName + "-" + String.valueOf((Long.MAX_VALUE - ts) / 5000));
    }

    public static byte[] getQualOfServiceSpanName(ThriftSpan thriftSpan) {
        return Bytes.toBytes(thriftSpan.getTraceId());
    }

    public static byte[] getQualOfServiceName(ThriftSpan thriftSpan) {
        return Bytes.toBytes(thriftSpan.getTraceId());
    }

}
