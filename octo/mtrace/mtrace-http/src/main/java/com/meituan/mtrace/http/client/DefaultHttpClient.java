package com.meituan.mtrace.http.client;

import com.meituan.mtrace.http.TraceHttpProcessor;
import org.apache.http.annotation.ThreadSafe;

/**
 * User: YangXuehua
 * Date: 14-1-8
 * Time: 下午3:33
 *
 * @since httpcomponents httpclient 4.0
 */
@ThreadSafe
public class DefaultHttpClient extends org.apache.http.impl.client.DefaultHttpClient {
    public DefaultHttpClient(org.apache.http.conn.ClientConnectionManager conman, org.apache.http.params.HttpParams params) {
        super(conman, params);
        addMtraceInterceptor();
    }

    public DefaultHttpClient(org.apache.http.conn.ClientConnectionManager conman) {
        super(conman);
        addMtraceInterceptor();
    }

    public DefaultHttpClient(org.apache.http.params.HttpParams params) {
        super(params);
        addMtraceInterceptor();
    }

    public DefaultHttpClient() {
        super();
        addMtraceInterceptor();
    }

    public DefaultHttpClient(String appkey) {
        super();
        addMtraceInterceptor(appkey);
    }

    private void addMtraceInterceptor(String appkey) {
        TraceHttpProcessor traceHttpProcessor = new TraceHttpProcessor();
        traceHttpProcessor.initAppkey(appkey);
        this.addRequestInterceptor(traceHttpProcessor);
        this.addResponseInterceptor(traceHttpProcessor);
    }

    private void addMtraceInterceptor() {
        TraceHttpProcessor traceHttpProcessor = new TraceHttpProcessor();
        this.addRequestInterceptor(traceHttpProcessor);
        this.addResponseInterceptor(traceHttpProcessor);
    }
}
