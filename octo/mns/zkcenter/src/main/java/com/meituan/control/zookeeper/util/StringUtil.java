package com.meituan.control.zookeeper.util;

/**
 * User: jinmengzhe
 * Date: 2015-05-22
 */
public class StringUtil {
    public static boolean isEmpty(String ss) {
        return (ss == null || ss.trim().isEmpty());
    }
}
