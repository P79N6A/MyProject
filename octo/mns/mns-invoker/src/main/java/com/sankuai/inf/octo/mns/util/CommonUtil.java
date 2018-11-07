package com.sankuai.inf.octo.mns.util;

/**
 * Description: not recommended to use, except for mns-invoker itself.
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CommonUtil {
    // please do not change it to multi thread.
    public static ScheduledExecutorService mnsCommonSchedule = Executors.newSingleThreadScheduledExecutor(new ScheduleTaskFactory("MnsInvoker-Schedule"));

    private CommonUtil() {

    }

    public static BufferedInputStream wrapToStream(byte[] data) {
        return new BufferedInputStream(new ByteArrayInputStream(data));
    }

    public static boolean isBlankString(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean isEmtpy(List<?> list) {
        return null == list || list.isEmpty();
    }

    public static boolean containsIgnoreCase(String str, String searchStr) {
        return null != str && null != searchStr && str.toLowerCase().contains(searchStr);
    }

    static String trim(String str) {
        return null == str ? null : str.trim();
    }
}
