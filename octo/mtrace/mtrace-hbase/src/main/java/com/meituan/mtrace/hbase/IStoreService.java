package com.meituan.mtrace.hbase;


import com.meituan.mtrace.common.Span;
import com.meituan.mtrace.thriftjava.*;

import java.util.List;

/**
 * @author zhangzhitong
 * @created 9/15/15
 */
public interface IStoreService {
    /**
     * 将data存储到hbase中，包括数据存储和索引建立
     * @param data thrift 序列化的二进制序列
     */
    public void store(byte[] data);
    public void store(List<ThriftSpan> thriftSpans);
    public void store(ThriftSpan thriftSpan);

    /**
     * 通过traceId 获取它的ThriftSpan序列
     * @param traceId traceId 全局id
     * @return span list
     */
    public List<Span> getSpansByTraceId(long traceId, Env env);

    /**
     * 通过traceIds 获取他们各自的ThriftSpan序列
     * @param traceIds traceId list
     * @return trace list
     */
    public List<List<Span>> getSpansByTraceIds(List<Long> traceIds, Env env);

    public List<Long> getTraceIdsByServiceName(String serviceName, long endTs, int limit, Env env);
    /**
     * 通过服务名 + 结束时间 + limit 获取符合条件的TraceId序列
     * @param serviceName service name
     * @param serviceSpanName span name
     * @param endTs 时间戳
     * @param limit 最近几个时间单元
     * @return trace id list
     */
    public List<Long> getTraceIdsByServiceSpanName(String serviceName, String serviceSpanName, long endTs, int limit, Env env);

    /**
     * 通过服务名 + 结束时间 + limit 获取符合条件的spans
     * @param serviceName service name : appkey
     * @param serviceSpanName span name
     * @param endTs 时间戳
     * @param limit 最近几个时间单元
     * @return trace list
     */
    public List<List<Span>> getSpansByServiceSpanName(String serviceName, String serviceSpanName, long endTs, int limit, Env env);
}
