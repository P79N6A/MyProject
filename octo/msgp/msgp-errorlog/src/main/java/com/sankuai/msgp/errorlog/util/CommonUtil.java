package com.sankuai.msgp.errorlog.util;

public class CommonUtil {

    public static Integer getMinuteStart(int seconds) {
        int minuteStart = seconds / 60 * 60;
        return minuteStart;
    }
}
