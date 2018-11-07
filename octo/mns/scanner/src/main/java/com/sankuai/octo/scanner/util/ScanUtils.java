package com.sankuai.octo.scanner.util;

import com.sankuai.inf.octo.mns.ProcessInfoUtil;
import org.apache.commons.lang.StringUtils;


public class ScanUtils {

    public static String hostIpPrefix = "";

    static {
        if (StringUtils.isBlank(hostIpPrefix))
            hostIpPrefix = getLocalIpV4Prefix();
    }

    public static String getLocalIpV4Prefix() {
        String localAddress = ProcessInfoUtil.getLocalIpV4();
        String[] ipSegs = localAddress.split("\\.");
        String prefix = ipSegs[0] + "." + ipSegs[1] + ".";
        return prefix;
    }

    public static boolean isSameDC(String remoteIp) {
        return remoteIp.startsWith(hostIpPrefix);

    }

    public static String getProviderIpPrefix(String provider) {
        String[] ipSegs = provider.split("\\.");
        String prefix = ipSegs[0] + "." + ipSegs[1] + ".";
        return prefix;
    }

}
