package com.meituan.mtrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhangxi
 * @created 13-11-4
 */
public class HttpInvoker {
    private final static Logger LOG = LoggerFactory.getLogger(HttpInvoker.class);

    public void invoke(String apikey, Object param) {
        ClientTracer tracer = Tracer.getClientTracer();
        tracer.startNewSpan(apikey, new Endpoint("test", "127.0.0.1", 8031));
        tracer.setClientSent(new Endpoint("mtpoi", "10.2.2.3", 8022));
        LOG.info("HttpInvoker call {} from {} to {} with trace {},{},{} param {}",
                new Object[]{apikey, tracer.getSpan().getLocal(),
                        tracer.getSpan().getRemote(), tracer.getSpan().getTraceId(),
                        tracer.getSpan().getSpanId(), tracer.getSpan().getSpanName(), param});
        tracer.setClientReceived(0);
    }
}
