package com.sankuai.octo.test.utils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetHelper {
    private NetHelper() {
    }

    public static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    public static String getRealIp(HttpServletRequest request) {
        String ip = head(request, "X-Real-IP");
        if (ip != null && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = head(request, "X-Forwarded-For");
        if (ip != null) {
            int index = ip.indexOf(',');
            return (index != -1) ? ip.substring(0, index) : ip;
        }
        if (ip == null || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip == null ? "unkown" : ip;
    }

    private static String head(HttpServletRequest req, String s) {
        return req.getHeader(s);
    }
}
