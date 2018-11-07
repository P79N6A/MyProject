package com.meituan.mtrace.http;

import com.meituan.mtrace.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.resource.UniformResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhangxi
 * @created 14-1-2
 */
@Aspect
public class TraceRestletAspect {
    private static final Logger LOG = LoggerFactory.getLogger(TraceRestletAspect.class);
    private static final String ORG_RESTLET_HTTP_HEADERS = "org.restlet.http.headers";

    static {
        LogSpanCollector.setEnableOcto(true);
    }

    @Pointcut("@annotation(org.restlet.resource.Get) || @annotation(org.restlet.resource.Post) || " +
            "@annotation(org.restlet.resource.Put)  || @annotation(org.restlet.resource.Delete)")
    public void restletMethod() {
    }

    @Around("restletMethod()")
    public Object trace(ProceedingJoinPoint point) throws Throwable {
        long t1 = 0;
        long t2 = 0;
        long t3;
        long t4;
        if (LOG.isDebugEnabled()) {
            t1 = System.nanoTime();
        }
        ServerTracer tracer = Tracer.getServerTracer();
        tracer.clearCurrentSpan();
        int code = Helper.FAIL;
        try {
            try {
                if (LOG.isDebugEnabled()) {
                    t2 = System.nanoTime();
                }
                String spanname = Helper.getSpanname(point);
                if (LOG.isDebugEnabled()) {
                    t3 = System.nanoTime();
                    LOG.debug("generate spanname : " + (t3 - t2));
                }
                if (point.getTarget() instanceof UniformResource) {
                    UniformResource obj = (UniformResource) point.getTarget();
                    String traceId = Helper.getField(obj.getRequest(), FieldType.TraceId.getName());
                    String spanId = Helper.getField(obj.getRequest(), FieldType.SpanId.getName());
                    Boolean sample = Boolean.valueOf(Helper.getField(obj.getRequest(), FieldType.Sample.getName()));
                    Boolean debug = Boolean.valueOf(Helper.getField(obj.getRequest(), FieldType.Debug.getName()));
                    if (!StringUtil.isBlank(traceId)) {
                        tracer.setCurrentTrace(traceId, spanId, spanname, Helper.getDefaultLocal(), sample, debug);
                        try {
                            // 通过header返回服务端信息
                            Form responseHeaders = (Form) obj.getResponse().getAttributes().get(ORG_RESTLET_HTTP_HEADERS);
                            if (responseHeaders == null) {
                                responseHeaders = new Form();
                                obj.getResponse().getAttributes().put(ORG_RESTLET_HTTP_HEADERS, responseHeaders);
                            }
                            responseHeaders.add(FieldType.SpanName.getName(), spanname);
                            responseHeaders.add(FieldType.Appkey.getName(), tracer.getSpan().getLocal().getAppkey());
                            responseHeaders.add(FieldType.Host.getName(), tracer.getSpan().getLocal().getHost());
                        } catch (Exception e) {
                            LOG.warn("set responseHeaders fail...", e);
                        }
                    } else {
                        // 无traceId，说明对端未接入，无需回传信息
                        tracer.setCurrentTrace(spanname, Helper.getDefaultLocal(), sample, debug);
                    }
                    Endpoint remoteEndPoint = remote(obj.getRequest());
                    tracer.setServerReceived(remoteEndPoint);
                    if (LOG.isDebugEnabled()) {
                        t4 = System.nanoTime();
                        LOG.debug("setServerReceived total : " + (t4 - t1));
                    }
                } else {
                    LOG.warn("{} in not instance of UniformResource", point.getTarget());
                }
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
            Object response = point.proceed(point.getArgs());
            code = 0;
            return response;
        } finally {
            tracer.setServerSend(code);
        }
    }

    private Endpoint remote(Request request) {
        String auth = (String) request.getAttributes().get("Authorization");
        String clientId = "";
        if (auth != null) {
            clientId = auth.substring(auth.indexOf(' ') + 1, auth.indexOf(':'));
        }
        String remoteIp = request.getClientInfo().getAddress();
        int remotePort = request.getClientInfo().getPort();
        return new Endpoint(clientId, remoteIp, remotePort);
    }
}
