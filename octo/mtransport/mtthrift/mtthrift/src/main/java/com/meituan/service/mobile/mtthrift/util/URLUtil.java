package com.meituan.service.mobile.mtthrift.util;

import org.apache.commons.lang.StringUtils;

public class URLUtil {

    public static String getURIPath(String uri) {
        if (StringUtils.isBlank(uri)) {
            return "";
        }
        int pathEndPos = uri.indexOf('?');
        if (pathEndPos < 0) {
            return uri;
        } else {
            return uri.substring(0, pathEndPos);
        }
    }
}
