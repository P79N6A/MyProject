package com.meituan.service.mobile.mtthrift.util;

import com.meituan.mtrace.Span;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.mtrace.RequestHeader;
import org.apache.commons.lang.StringUtils;

/**
 * Author: caojiguang@gmail.com
 * Date: 15/9/24
 * Description:
 */
public class ClientInfoUtil {

    public static String getClientIp(){
        String ip = "";
        RequestHeader requestInfo = MtraceServerTBinaryProtocol.requestHeaderInfo.get();
        if(null != requestInfo){
            ip = requestInfo.getClientIp();
        }
        return ip;


    }

    public static String getClientAppKey() {
        String appKey = "";
        RequestHeader requestInfo = MtraceServerTBinaryProtocol.requestHeaderInfo.get();
        if(null != requestInfo){
            appKey = requestInfo.getClientAppkey();
        }
        return appKey;

    }

    // clientProxy.getServiceSimpleName() + "." + methodName

    public static String getRequestServiceName() {
        String requestServiceName = "";
        RequestHeader requestInfo = MtraceServerTBinaryProtocol.requestHeaderInfo.get();
        if(null != requestInfo){
            String spanName = requestInfo.getSpanName();
            if( !StringUtils.isBlank(spanName) && spanName.split("\\.").length > 1) {
                requestServiceName = spanName.split("\\.")[1];
            }
        }
        return requestServiceName;

    }

    /**
     *  在调用端获取traceId,必须在调用端埋点后调用，否则会返回null
     * @return
     */
    public static String getClientTracerTraceId() {
        Span clientSpan = Tracer.getClientTracer().getSpan();
        return clientSpan == null ? null : clientSpan.getTraceId();
    }
}
