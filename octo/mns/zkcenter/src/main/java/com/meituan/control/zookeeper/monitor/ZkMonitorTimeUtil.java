package com.meituan.control.zookeeper.monitor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: jinmengzhe
 * Date: 2015-07-24
 *
 * 时间取整根据需求自定义的 不具有通用性 以比较丑陋的方式实现就行
 *
 */
public class ZkMonitorTimeUtil {
    public static Date get5MinutesAfter(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(Calendar.MINUTE, 5);
        return calendar.getTime();
    }

    public static Date get1HourAfter(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(Calendar.HOUR_OF_DAY, 1);
        return calendar.getTime();
    }

    public static Date get1DayAfter(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    // 获取一个时间往前、最近的五分钟Date
    public static Date getNearestDate4FiveMinutes(Date date) throws Exception {
        SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ss = DF.format(date);
        String prefixKeep = ss.substring(0, 15);
        int minute = Integer.parseInt(ss.substring(15, 16));
        if (minute >= 5) {
            ss = prefixKeep + "5:00";
        } else {
            ss = prefixKeep + "0:00";
        }

        return DF.parse(ss);
    }

    // 获取一个时间往前最近的半小时Date
    public static Date getNearestDate4HalfHour(Date date) throws Exception {
        SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ss = DF.format(date);
        String prefixKeep = ss.substring(0, 14);
        int minute = Integer.parseInt(ss.substring(14, 16));
        if (minute >= 30) {
            ss = prefixKeep + "30:00";
        } else {
            ss = prefixKeep + "00:00";
        }

        return DF.parse(ss);
    }

    // 获取一个时间往前 最近的一天Date--从0点开始
    public static Date getNearestDate4Day(Date date) throws Exception {
        SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String ss = DF.format(date);
        String prefixKeep = ss.substring(0, 11);
        ss = prefixKeep + "00:00:00";

        return DF.parse(ss);
    }

    // 5分钟的整数倍
    public static Date getDate1HourBefore() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        Date date = calendar.getTime();

        return getNearestDate4FiveMinutes(date);
    }

    // 5分钟整数倍
    public static Date getDate1DayBefore() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date date = calendar.getTime();

        return getNearestDate4FiveMinutes(date);
    }

    // 半小时整数倍
    public static Date getDate1WeekBefore() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date date = calendar.getTime();

        return getNearestDate4HalfHour(date);
    }

    // 半小时整数倍
    public static Date getDate1MonthBefore() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date date = calendar.getTime();

        return getNearestDate4HalfHour(date);
    }

    public static void main(String[] args) {
        try {
            System.out.println(getDate1HourBefore());
            System.out.println(getDate1DayBefore());
            System.out.println(getDate1WeekBefore());
            System.out.println(getDate1MonthBefore());
            System.out.println(getNearestDate4Day(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
