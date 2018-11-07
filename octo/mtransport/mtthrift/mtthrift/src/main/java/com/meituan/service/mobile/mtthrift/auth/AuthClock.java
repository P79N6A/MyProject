package com.meituan.service.mobile.mtthrift.auth;

public class AuthClock {
    private static volatile long now = System.currentTimeMillis();

    public static long currentTimeMillis() {
        return now;
    }

    public static void updateNow() {
        now = System.currentTimeMillis();
    }
}
