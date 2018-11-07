package com.meituan.service.mobile.mtthrift.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: caojiguang@gmail.com
 * Date: 16/8/15
 * Description:
 */
@Deprecated
public class ContextUtil {

    private static ThreadLocal<Map<String, String>> globalContext = new ThreadLocal<Map<String, String>>();
    private static ThreadLocal<Map<String, String>> requestContext = new ThreadLocal<Map<String, String>>();
    private static ThreadLocal<Map<String, String>> localContext = new ThreadLocal<Map<String, String>>();

    /**
     * 单次消息上下文，context中包含的entry数不超过20
     * @param context
     */
    public static void setLocalContext(Map<String, String> context) {
        if(null == context || 0 == context.size() )
            return;
        localContext.set(null);
        Map<String, String> local = new HashMap<String, String>();
        local.putAll(context);
        localContext.set(local);
    }

    //单次消息上下文，限制总大小不超过 2K Bytes
    public static void setRequestContext(Map<String, String> context) {

    }

    //全链路消息上下文，限制总大小不超过 512 Bytes
    public static void setGlobalContext(Map<String, String> context) {

    }

    public static Map<String, String> getLocalContext() {
        return localContext.get();
    }

    public static Map<String, String> getGlobalContext() {
        return globalContext.get();
    }

    public static Map<String, String> getRequestContext() {
        return requestContext.get();
    }

    public static void clearContext() {
        localContext.remove();
        globalContext.remove();
        requestContext.remove();
    }

    public static void clearRequestContext () {
        requestContext.remove();
    }
}
