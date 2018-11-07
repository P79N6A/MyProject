package com.meituan.mtrace;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhangzhitong
 * @created 1/28/16
 */
public class ServerTracerTest {
    private Logger logger = LoggerFactory.getLogger(ServerTracerTest.class);
    @Before
    public void setUp() {
    }
    /**
     * server 端作为头节点
     */
    @Test
    public void testHeadTrace() {
        TraceParam param = new TraceParam("testHeadTrace");
        ServerTracer.getInstance().record(param);
        Span span = ServerTracer.getInstance().getSpan();
        logger.info(span.toString());
    }

    /**
     * server 端作为下游节点
     */
    @Test
    public void testExtendTrace() {
        TraceParam param = new TraceParam("testExtendTrace");
        param.setTraceId("123456");
        param.setSpanId("0.1.2");
        param.setDebug(false);
        ServerTracer.getInstance().record(param);
        Span span = ServerTracer.getInstance().getSpan();
        logger.info(span.toString());

    }

    @Test
    public void testExtendTraceNoSpanId() {
        TraceParam param = new TraceParam("testExtendTraceNoSpanId");
        param.setTraceId("123456");
        ServerTracer.getInstance().record(param);
        Span span = ServerTracer.getInstance().getSpan();
        logger.info(span.toString());
    }

    @Test
    public void clearSpan() {
        ClientTracer.getInstance().record(new TraceParam("clearSpan"));
        ClientTracer.getInstance().clearCurrentSpan();
        Span span = ClientTracer.getInstance().getSpan();
        System.out.println(span);
    }
    @Test
    public void testParam() {
        String spanName = "ClassName.methodName";
        String localAppKey = "com.meituan.mtrace";
        String localIp = "127.0.0.1";
        int localPort = 20;
        String remoteAppKey = "xxx";
        String remoteIp = "127.0.0.2";
        int remotePort = 80;
        String infraName = "mtthrift";
        String version = "1.5.8";
        int size = 1024;

        TraceParam param = new TraceParam(spanName);
        param.setLocal(localAppKey, localIp, localPort);
        param.setRemote(remoteAppKey, remoteIp, remotePort);
        param.setInfraName(infraName);
        param.setVersion(version);
        param.setPackageSize(size);
        Span span = Tracer.serverRecv(param);
        span.setStatus(Tracer.STATUS.DROP);
        Tracer.serverSend();

    }
}
