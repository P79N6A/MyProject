package com.meituan.mtrace;

/**
 * @author zhangxi
 * @created 13-11-5
 */
@Deprecated
public class StringUtil {

    public static boolean isBlank(CharSequence cs) {
        return Validate.isBlank(cs);
    }
}
