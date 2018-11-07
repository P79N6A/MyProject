package com.meituan.mtrace;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class ClientTracerTest {
    Logger logger  = LoggerFactory.getLogger(ClientTracerTest.class);
    @Before
    public void setUp() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    /**
     * client 端作为头节点
     */
    @Test
    public void testHeadTrace() {
        TraceParam param = new TraceParam("testHeadTrace");
        Tracer.clientSend(param);
        Span span = Tracer.clientRecv();
        assertEquals(span.getSpanId(), "0");
        logger.info(span.toString());
    }

    /**
     * client 端作为下游节点
     */
    @Test
    public void testExtendTrace() {
        TraceParam param = new TraceParam("testExtendTrace");
        Tracer.serverRecv(param);
        Tracer.clientSend(param);
        Span span = Tracer.clientRecv();
        Span serverSpan = Tracer.serverSend();
        assertEquals(span.getSpanId(), "0.1");
        assertEquals(span.getTraceId(), serverSpan.getTraceId());
        logger.info(span.toString());

    }

    @Test
    public void clearSpan() {
        ClientTracer.getInstance().record(new TraceParam("clearSpan"));
        ClientTracer.getInstance().clearCurrentSpan();
        Span span = ClientTracer.getInstance().getSpan();
        assertEquals(span, null);
    }

    @Test
    public void testParam() {
        String spanName = "ClientTracer.testParam";
        String localAppKey = "com.meituan.mtrace.Test";
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
        Span span = Tracer.clientSend(param);
        span.setStatus(Tracer.STATUS.EXCEPTION);
        span = Tracer.clientRecv();
        assertEquals(span.getSpanName(), spanName);
        assertEquals(span.getLocalAppKey(), localAppKey);
        logger.info(span.toString());
    }

    @Test
    public void testAsync() {
        String spanName = "ClientTracer.testAsync";
        String localAppKey = "com.meituan.mtrace.Test";
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
        Span span = Tracer.clientSendAsync(param);
        span.setStatus(Tracer.STATUS.DROP);
        span = Tracer.clientRecvAsync(span);
        logger.info(span.toString());
    }
}
