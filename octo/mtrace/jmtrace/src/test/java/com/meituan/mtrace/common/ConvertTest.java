package com.meituan.mtrace.common;

import com.meituan.mtrace.Convert;
import com.meituan.mtrace.Span;
import com.meituan.mtrace.Tracer;
import com.meituan.mtrace.thrift.model.ThriftSpan;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhangzhitong
 * @created 2/23/16
 */
public class ConvertTest {
    Logger logger  = LoggerFactory.getLogger(ConvertTest.class);
    @Test
    public void spanToThriftTest() {
        Span span = new Span("spanToThriftTest");
        span.setLocal("local", "127.0.0.1", 80);
        span.setInfraName("http");
        span.setStatus(Tracer.STATUS.SUCCESS);
        ThriftSpan thriftSpan = Convert.spanToThrift(span);
        assert thriftSpan != null;
        logger.info(thriftSpan.toString());
    }
    @Test
    public void testMask() {
        int mask = 0;
        Assert.assertFalse(Convert.isDebug(mask));
        Assert.assertFalse(Convert.isAsync(mask));
        Convert.isAsync(mask);
        mask = Convert.returnDebugMask(mask);
        mask = Convert.returnAsyncMask(mask);
        Assert.assertTrue(Convert.isDebug(mask));
        Assert.assertTrue(Convert.isAsync(mask));
    }

}
