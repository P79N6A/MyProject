package com.sankuai.octo.doclet.util;

public class StringUtil {

    public static boolean isBlank(Object obj) {
        if (obj == null)
            return true;
        if (obj instanceof String) {
            String str = (String) obj;
            return "".equals(str.trim());
        }
        try {
            String str = String.valueOf(obj);
            return str == null ? true : "".equals(str.trim());
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isNotBlank(Object obj) {
        return !isBlank(obj);
    }
}
