package com.meituan.service.mobile.mtthrift.util;


public class ThreadLocalUtil {

    private static final ThreadLocal<String> serviceSimpleName = new ThreadLocal<String>();
    private static final ThreadLocal<String> serviceCompleteSimpleName = new ThreadLocal<String>();
    private static final ThreadLocal<String> isUnifiedProto = new ThreadLocal<String>();
    public static final ThreadLocal<Boolean> authGray = new ThreadLocal<Boolean>();

    public static void clear() {
        removeServiceCompleteName();
        removeServiceSimpleName();
        removeIsUnifiedProto();
        removeAuthGray();
    }

    public static String getServiceSimpleName() {
        return serviceSimpleName.get();
    }

    public static void setServiceSimpleName(String simpleName) {
        serviceSimpleName.set(simpleName);
    }

    private static void removeServiceSimpleName() {
        serviceSimpleName.remove();
    }

    // 全类名，统一鉴权使用
    public static String getServiceCompleteName() {
        return serviceCompleteSimpleName.get();
    }

    public static void setServiceCompleteName(String completeName) {
        serviceCompleteSimpleName.set(completeName);
    }

    private static void removeServiceCompleteName() {
        serviceCompleteSimpleName.remove();
    }

    public static ThreadLocal<String> getServiceCompleteSimpleName() {
        return serviceCompleteSimpleName;
    }

    public static String getIsUnifiedProto() {
        return isUnifiedProto.get() == null ? "empty" : isUnifiedProto.get();
    }

    private static void removeIsUnifiedProto() {
        isUnifiedProto.remove();
    }

    public static void setIsUnifiedProto(String unifiedProtoValue) {
        isUnifiedProto.set(unifiedProtoValue);
    }

    public static void setAuthGray(boolean gray) {
        authGray.set(gray);
    }

    public static Boolean getAuthGray() {
        return authGray.get();
    }

    public static void removeAuthGray() {
        authGray.remove();
    }
}
