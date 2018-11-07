package com.sankuai.msgp.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zava on 16/1/26.
 */
public class DateTimeUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DateTimeUtil.class);

    public static final Long DAY_TIME = 86400000L;
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_DAY_FORMAT = "yyyy-MM-dd";
    public static final String START_DATE_OF_UNIX = "1970-01-01";
    public static final String START_TIME_OF_UNIX = "1970-01-01 00:00:00";

    /**
     * 锁对象
     */
    private static final Object lock = new Object();

    /**
     * 存放不同的日期模板格式的sdf的Map
     */
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    /**
     * 是用ThreadLocal<SimpleDateFormat>来获取SimpleDateFormat,这样每个线程只会有一个SimpleDateFormat
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String format(Date date, String pattern) {
        return getSimpleDateFormat(pattern).format(date);
    }

    public static Date parse(String dateStr, String pattern) {
        try {
            return getSimpleDateFormat(pattern).parse(dateStr);
        } catch (ParseException e) {
            LOG.warn("Error dateStr:" + dateStr + ",pattern:" + pattern);
        }
        return null;
    }

    public static String getYesterday() {
        return getLastOneDayDate(-1);
    }

    public static String formatDate(long seconds) {
        return getSimpleDateFormat(DATE_TIME_FORMAT).format(new Date(seconds * 1000L));
    }

    public static Long getTimeInSecond(String pattern, String timeStr) {
        try {
            return getSimpleDateFormat(pattern).parse(timeStr).getTime() / 1000;
        } catch (ParseException e) {
            return 0l;
        }
    }

    public static String getLastOneDayDate(int todayInterval) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, todayInterval);
        return getSimpleDateFormat(DATE_DAY_FORMAT).format(cal.getTime());
    }

    /**
     * 返回一个ThreadLocal的SimpleDateFormat,每个线程只会new一次sdf
     *
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSimpleDateFormat(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);

        // 此处的双重判断和同步是为了防止sdfMap这个单例被多次put重复的sdf
        if (tl == null) {
            synchronized (lock) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    // 只有Map中还没有这个pattern的sdf才会生成新的sdf并放入map
                    // 这里是关键,使用ThreadLocal<SimpleDateFormat>替代原来直接new SimpleDateFormat
                    tl = new ThreadLocal<SimpleDateFormat>() {
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, tl);
                }
            }
        }
        return tl.get();
    }
}
