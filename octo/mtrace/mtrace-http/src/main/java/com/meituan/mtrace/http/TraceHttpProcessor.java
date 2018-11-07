package com.meituan.mtrace.http;

import com.meituan.mtrace.*;
import org.apache.http.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author zhangxi
 * @created 14-1-1
 */
public class TraceHttpProcessor implements HttpProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(TraceHttpProcessor.class);
    private Endpoint localEndpoint = Helper.getDefaultLocal();

    static {
        LogSpanCollector.setEnableOcto(true);
    }

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        try {
            ClientTracer tracer = Tracer.getClientTracer();
            tracer.startNewSpan(getSpanname(request), getLocalEndpoint());
            Endpoint remoteEndpoint = new Endpoint(getRemoteHost(request), "", Helper.DEFAULT_HTTP_PORT);
            tracer.setClientSent(remoteEndpoint);
            request.setHeader(FieldType.TraceId.getName(), tracer.getSpan().getTraceId());
            request.setHeader(FieldType.SpanId.getName(), tracer.getSpan().getSpanId());
            if (tracer.getSpan().getSample() != null) {
                request.setHeader(FieldType.Sample.getName(), tracer.getSpan().getSample().toString());
            }
            if (tracer.getSpan().getDebug() != 0) {
                request.setHeader(FieldType.Debug.getName(), "true");
            }
            // TODO: 通过header传递appkey、host、port有些安全风险？
        } catch (Exception e) {
            LOG.warn("", e);
        }
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        ClientTracer tracer = Tracer.getClientTracer();
        Header spannameHeader = response.getFirstHeader(FieldType.SpanName.getName());
        if (spannameHeader != null && !StringUtil.isBlank(spannameHeader.getValue())) {
            tracer.getSpan().setSpanName(spannameHeader.getValue());
        }
        Header appkeyHeader = response.getFirstHeader(FieldType.Appkey.getName());
        if (appkeyHeader != null && !StringUtil.isBlank(appkeyHeader.getValue())) {
            tracer.getSpan().getRemote().setAppkey(appkeyHeader.getValue());
        }
        Header hostHeader = response.getFirstHeader(FieldType.Host.getName());
        if (hostHeader != null && !StringUtil.isBlank(hostHeader.getValue())) {
            tracer.getSpan().getRemote().setHost(hostHeader.getValue());
        }
        tracer.setClientReceived(response.getStatusLine().getStatusCode());
    }


    @Deprecated  // Rest风格会导致spanname过多
    private String getSpanname(HttpRequest request) {
        String uri = request.getRequestLine().getUri();
        int index = uri.indexOf('?');
        return index < 0 ? uri : uri.substring(0, index);
    }

    private String getRemoteHost(HttpRequest request) {
        Header[] headers = request.getAllHeaders();
        for (int i = 0; i < headers.length; i++) {
            if ("HOST".equalsIgnoreCase(headers[i].getName())) {
                return "http://" + headers[i].getValue();
            }
        }
        return null;
    }

    public Endpoint getLocalEndpoint() {
        return localEndpoint;
    }

    public void setLocalEndpoint(Endpoint localEndpoint) {
        this.localEndpoint = localEndpoint;
    }

    public void initAppkey(String appkey) {
        if (!StringUtil.isBlank(appkey)) {
            localEndpoint = new Endpoint(appkey, Helper.getLocalIp(), 0);
        }
    }
}
