package com.meituan.mtrace.http;

import com.meituan.mtrace.*;
import org.apache.commons.collections.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * @author zhangxi
 * @created 13-12-4
 */
public class TraceHandlerInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(TraceHandlerInterceptor.class);
    private static final Map HANDLER_SPANNAME_LRU_MAP = Collections.synchronizedMap(new LRUMap(10000));
    private String appkey;
    private int port;

    @PostConstruct
    public void init() {
        Helper.initLocal(appkey, port);
        LogSpanCollector.setEnableOcto(true);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String action = request.getRequestURI().substring(request.getContextPath().length());
            if (!Helper.matchExclude(action)) {
                String spanname;
                long t1 = 0;
                long t2 = 0;
                long t3;
                long t4;
                if (LOG.isDebugEnabled()) {
                    t1 = System.nanoTime();
                }
                int hashCode = handler.hashCode();
                spanname = (String) HANDLER_SPANNAME_LRU_MAP.get(hashCode);
                if (spanname == null) {
                    if (LOG.isDebugEnabled()) {
                        t2 = System.nanoTime();
                    }
                    if (HandlerMethod.class.isAssignableFrom(handler.getClass())) {
                        HandlerMethod handlerMethod = (HandlerMethod) handler;
                        spanname = generate(handlerMethod);
                        hashCode = handlerMethod.hashCode();
                        HANDLER_SPANNAME_LRU_MAP.put(hashCode, spanname);
                    } else {
                        spanname = action;
                        HANDLER_SPANNAME_LRU_MAP.put(hashCode, spanname);
                    }
                    if (LOG.isDebugEnabled()) {
                        t3 = System.nanoTime();
                        LOG.debug("generate spanname : " + spanname + ", time " + (t3 - t2) + ", hash code " + hashCode + ", handler " + handler.getClass().getName());
                    }
                }
                ServerTracer tracer = Tracer.getServerTracer();
                tracer.clearCurrentSpan();
                String traceId = Helper.getField(request, FieldType.TraceId.getName());
                String spanId = Helper.getField(request, FieldType.SpanId.getName());
                Boolean sample = Boolean.valueOf(Helper.getField(request, FieldType.Sample.getName()));
                Boolean debug = Boolean.valueOf(Helper.getField(request, FieldType.Debug.getName()));
                if (!StringUtil.isBlank(traceId)) {
                    tracer.setCurrentTrace(traceId, spanId, spanname, Helper.getDefaultLocal(), sample, debug);
                    response.setHeader(FieldType.Appkey.getName(), tracer.getSpan().getLocal().getAppkey());
                    if (spanname != null) {
                        response.setHeader(FieldType.SpanName.getName(), spanname);
                    }
                    response.setHeader(FieldType.Host.getName(), tracer.getSpan().getLocal().getHost());
                } else {
                    // 无traceId，说明对端未接入，无需回传信息
                    tracer.setCurrentTrace(spanname, Helper.getDefaultLocal(), sample, debug);
                }
                Endpoint remoteEndPoint = Helper.getRemote(request);
                tracer.setServerReceived(remoteEndPoint);
                if (LOG.isDebugEnabled()) {
                    t4 = System.nanoTime();
                    LOG.debug("gen spanname total : " + spanname + ", time " + (t4 - t1));
                }
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            String action = request.getRequestURI().substring(request.getContextPath().length());
            if (!Helper.matchExclude(action)) {
                ServerTracer tracer = Tracer.getServerTracer();
                long t1 = 0;
                long t2;
                if (LOG.isDebugEnabled()) {
                    t1 = System.nanoTime();
                }
                // TODO response set sample/debug header?
                tracer.setServerSend(ex == null ? 0 : Helper.FAIL);
                if (LOG.isDebugEnabled()) {
                    t2 = System.nanoTime();
                    LOG.debug("traceLog :" + (t2 - t1));
                }
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    private String generate(HandlerMethod handlerMethod) {
        return handlerMethod.getMethod().getDeclaringClass().getSimpleName() + "." +
                handlerMethod.getMethod().getName();
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
