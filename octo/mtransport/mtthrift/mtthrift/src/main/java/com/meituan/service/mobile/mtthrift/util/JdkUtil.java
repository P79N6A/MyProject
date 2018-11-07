package com.meituan.service.mobile.mtthrift.util;

/**
 * Author: caojiguang@gmail.com
 * Date: 15/12/25
 * Description:
 */
public class JdkUtil {

    public static boolean isJdk6() {
        boolean jdk6 = false;
        String version = System.getProperty("java.version");
        if (null != version && version.contains("1.6.")) {
            jdk6 = true;
        }

        return jdk6;
    }
}
