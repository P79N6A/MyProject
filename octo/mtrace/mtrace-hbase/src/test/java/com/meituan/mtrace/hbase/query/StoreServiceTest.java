package com.meituan.mtrace.hbase.query;

import com.meituan.mtrace.common.Span;
import com.meituan.mtrace.hbase.CloudTableStoreService;
import com.meituan.mtrace.hbase.Env;
import com.meituan.mtrace.thriftjava.*;
import junit.framework.TestCase;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zhangzhitong
 * @created 9/16/15
 */
public class StoreServiceTest extends TestCase {
    public CloudTableStoreService storeService = new CloudTableStoreService();

    public List<ThriftSpan> spanList = new ArrayList<ThriftSpan>();

    protected void setUp() {
        Random rnd = new Random();
        long traceId = rnd.nextLong();
        spanList.add(genThriftSpan(traceId, "0", "A"));
        spanList.add(genThriftSpan(traceId, "0.1", "B"));
        spanList.add(genThriftSpan(traceId, "0.2", "C"));
        spanList.add(genThriftSpan(traceId, "0.2.1", "D"));
        spanList.add(genThriftSpan(traceId, "0.2.2", "E"));
    }

    public void testStoreStorageTable() {
        System.out.println(spanList.toString());
        storeService.store(spanList);
    }

    private ThriftSpan genThriftSpan(long traceId, String spanId, String spanName) {
        ThriftSpan thriftSpan = new ThriftSpan();
        thriftSpan.setTraceId(traceId);
        Endpoint ep = new Endpoint();
        ep.setAppKey("com.meituan.mtrace.Test");
        thriftSpan.setRemote(ep);
        thriftSpan.setSpanId(spanId);
        thriftSpan.setSpanName(spanName);
        thriftSpan.setStart(System.currentTimeMillis());
        return thriftSpan;
    }

    public void testGetSpansByTraceId() {
        long traceId = -2810484237706037160L;
        try {
            System.out.println(storeService.getSpansByTraceId(traceId, Env.Prod));
        } catch (Exception e) {

        }

    }

    public void testGetTracesByServiceSpanName() {
        try {
            List<Long> traceIds = storeService.getTraceIdsByServiceSpanName("com.meituan.mtrace.Test", "A", System.currentTimeMillis(), 10, Env.Prod);
            System.out.println("total traceIds size is " + traceIds.size());
            List<List<Span>> traces = storeService.getSpansByTraceIds(traceIds, Env.Prod);
            if (!traces.isEmpty()) {
                for (List<Span> trace : traces) {
                    if (!trace.isEmpty()) {
                        System.out.println("-- TraceId : " + trace.get(0).getTraceId() + " Spans " + trace.size() + " --");
                        for (Span span : trace) {
                            System.out.println("    " + span.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {

        }

    }

    public void testGetTracesByServiceName() {
        try {
            List<Long> traceIds = storeService.getTraceIdsByServiceName("com.meituan.mtrace.Test", System.currentTimeMillis(), 10, Env.Prod);
            System.out.println("total traceIds size is " + traceIds.size());
            List<List<Span>> traces = storeService.getSpansByTraceIds(traceIds, Env.Prod);
            if (!traces.isEmpty()) {
                for (List<Span> trace : traces) {
                    if (!trace.isEmpty()) {
                        System.out.println("-- TraceId : " + trace.get(0).getTraceId() + " Spans " + trace.size() + " --");
                        for (Span span : trace) {
                            System.out.println("    " + span.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {

        }

    }

    public void testByteTraceId() {
        long traceId = 803720052400401581L;
        System.out.println("traceId : " + Bytes.toHex(Bytes.toBytes(traceId)));
    }

    public void testTime() {
        long x = 1445256234348L;
        System.out.println(Long.MAX_VALUE - x);
    }

}
