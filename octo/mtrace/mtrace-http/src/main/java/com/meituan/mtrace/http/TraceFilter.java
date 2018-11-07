package com.meituan.mtrace.http;

import com.meituan.mtrace.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TraceFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(TraceFilter.class);

    static {
        LogSpanCollector.setEnableOcto(true);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean shouldBeTraced = false;
        ServerTracer tracer = Tracer.getServerTracer();
        tracer.clearCurrentSpan();
        int code = Helper.FAIL;
        try {
            try {
                if (request instanceof HttpServletRequest) {
                    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
                    String action = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
                    if (!Helper.matchExclude(action)) {
                        String method = httpServletRequest.getMethod();
                        String spanName = action + "." + method;
                        String traceId = Helper.getField(httpServletRequest, FieldType.TraceId.getName());
                        String spanId = Helper.getField(httpServletRequest, FieldType.SpanId.getName());
                        Boolean sample = Boolean.valueOf(Helper.getField(httpServletRequest, FieldType.Sample.getName()));
                        Boolean debug = Boolean.valueOf(Helper.getField(httpServletRequest, FieldType.Debug.getName()));
                        if (!StringUtil.isBlank(traceId)) {
                            tracer.setCurrentTrace(traceId, spanId, spanName, Helper.getDefaultLocal(), sample, debug);
                        } else {
                            tracer.setCurrentTrace(spanName, Helper.getDefaultLocal(), sample, debug);
                        }
                        if (response instanceof HttpServletResponse && tracer.getSpan() != null) {
                            HttpServletResponse httpResponse = (HttpServletResponse) response;
                            httpResponse.setHeader(FieldType.SpanName.getName(), spanName);
                            httpResponse.setHeader(FieldType.Appkey.getName(), tracer.getSpan().getLocal().getAppkey());
                        }
                        Endpoint remoteEndPoint = Helper.getRemote(httpServletRequest);
                        tracer.setServerReceived(remoteEndPoint);
                        shouldBeTraced = true;
                    }
                }
            } catch (Exception e) {
                LOG.warn(e.getMessage());
            }
            chain.doFilter(request, response);
            code = 0;
        } finally {
            if (shouldBeTraced) {
                tracer.setServerSend(code);
            }
        }
    }

    @Override
    public void destroy() {
    }
}
