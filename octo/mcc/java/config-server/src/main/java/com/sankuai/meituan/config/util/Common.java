package com.sankuai.meituan.config.util;

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;

/**
 * Created by lhmily on 01/18/2016.
 */
public class Common {
    private static final boolean onlineOrNot = ProcessInfoUtil.isLocalHostOnline();
    private static final String localIP = ProcessInfoUtil.getLocalIpV4();
    private static String taskIP = isOnline() ? "10.32.190.219" : "10.5.239.189";

    public static boolean isOnline() {
        return onlineOrNot;
    }

    public static String getTaskIP() {
        return taskIP;
    }

    public static void setTaskIP(String taskIP) {
        Common.taskIP = taskIP;
    }

    public static String getLocalIP() {
        return localIP;
    }

    public static String getOctoUrl() {
        return isOnline() ? "http://octo.sankuai.com/" : "http://octo.test.sankuai.info/";
    }

    public static boolean isTaskIP() {
        String taskIP = getTaskIP();
        String localIP = getLocalIP();
        return taskIP.equals(localIP);
    }
}
