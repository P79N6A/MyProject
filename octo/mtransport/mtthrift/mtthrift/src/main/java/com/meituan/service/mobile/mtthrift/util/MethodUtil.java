package com.meituan.service.mobile.mtthrift.util;

import java.lang.reflect.Method;

public class MethodUtil {

    public static String generateMethodSignature(Method method) {
        return method.getName() + argumentTypesToString(method.getParameterTypes());
    }

    public static String generateMethodSignature(String methodName, Class<?>[] parameterTypes) {
        return methodName + argumentTypesToString(parameterTypes);
    }

    private static String argumentTypesToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(",");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }
}
