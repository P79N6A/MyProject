package com.meituan.service.mobile.mtthrift.util;

/**
 * Created by jiguang on 15/3/9.
 */
public class ExceptionUtil {


    public static String getExceptionMessage(Throwable e) {
        StackTraceElement[] stacks = e.getStackTrace();
        if (stacks != null && stacks.length > 0) {
            StackTraceElement stackTraceElement = stacks[0];
            return e.getClass().getName() + (e.getMessage() == null ?
                    "" :
                    ":" + e.getMessage()) + "(" + stackTraceElement
                    .getFileName() + "," + stackTraceElement.getMethodName()
                    + "() line " + stackTraceElement.getLineNumber() + ")";
        } else {
            return e.getClass().getName() + (e.getMessage() == null ?
                    "" :
                    ":" + e.getMessage());
        }
    }
}
