package com.meituan.mtrace.hbase.query;

import com.meituan.mtrace.common.Call;
import com.meituan.mtrace.common.Endpoint;
import com.meituan.mtrace.common.Span;
import junit.framework.TestCase;

/**
 * @author zhangzhitong
 * @created 11/13/15
 */
public class CallTest extends TestCase {
    private Span client = new Span();
    private Span server = new Span();

    public void setUp() {
        long traceId = 11111L;
        String spanId = "0.1";
        client.setTraceId(traceId);
        client.setSpanId(spanId);
        client.setSpanName("clientName");
        client.setClientEp(new Endpoint("MtraceClient", 1123412, (short) 8080));
        client.setServerEp(new Endpoint("ErrorTest", 0, (short) 0));
        client.setClientSide(true);

        server.setTraceId(traceId);
        server.setSpanId(spanId);
        server.setSpanName("serverName");
        server.setClientEp(new Endpoint("ErrorTest", 0, (short) 0));
        server.setServerEp(new Endpoint("MtraceServer", 4123412, (short) 9876));
        server.setClientSide(false);
    }

    public void merge(int cs, int cd, int ss, int sd) {
        client.setStart(cs);
        client.setDuration(cd);
        server.setStart(ss);
        server.setDuration(sd);
        Call call1 = new Call(client);
        call1.mergeSpan(server);
        Call call2 = new Call(server);
        call2.mergeSpan(client);
        System.out.println("Before Merge : client start " + cs + ", client duration " + cd +
                ", server start " + ss + ", server duration " + sd);
        System.out.println("After Merge : client merge server " + call1);
        System.out.println("After Merge : server merge client " + call2);
    }

    /**
     * client:  ---------------- 100, 50
     * server: ------------ 94, 40
     *              V
     * client: ---------------- 94, 50
     * server: ------------ 94, 40
     */
    public void testCallMerge1() {
        int cs = 100;
        int cd = 50;
        int ss = 94;
        int sd = 40;
        merge(cs, cd, ss, sd);
    }
    /**
     * client: ---------------- 100, 50
     * server:        ------------ 130, 40
     *              V
     * client:    ---------------- 120, 50
     * server:        ------------ 130, 40
     *
     */
    public void testCallMerge2() {
        int cs = 100;
        int cd = 50;
        int ss = 130;
        int sd = 40;
        merge(cs, cd, ss, sd);
    }

}
