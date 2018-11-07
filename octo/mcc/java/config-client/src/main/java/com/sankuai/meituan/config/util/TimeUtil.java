/*
 * Copyright (c) 2010-2011 meituan.com
 * All rights reserved.
 * 
 */
package com.sankuai.meituan.config.util;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhangxi
 * @version 1.0
 * @created 2012-12-24
 */
public class TimeUtil {
    public static final String DAY_FORMAT = "yyyy-MM-dd";
    public static final String HOUR_FORMAT = "yyyy-MM-dd HH";
    public static final String MINUTE_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String SECOND_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String[] PARSE_PATTERNS = new String[]{SECOND_FORMAT, MINUTE_FORMAT, HOUR_FORMAT, DAY_FORMAT};
    public static final int MINUTE_SECONDS = 60;
    public static final int HOUR_SECONDS = 3600;
    public static final int DAY_SECONDS = HOUR_SECONDS * 24;

    private TimeUtil() {
    }


    public static String getAuthDate(Date date) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(date);
    }
}
