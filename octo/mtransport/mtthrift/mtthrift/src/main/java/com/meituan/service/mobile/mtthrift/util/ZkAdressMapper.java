package com.meituan.service.mobile.mtthrift.util;

import org.apache.commons.lang.StringUtils;

/**
 * User: YangXuehua
 * Date: 14-4-11
 * Time: 下午4:31
 */
@Deprecated
public class ZkAdressMapper {
    private final static String ZK_OFFLINE_MOBILE_DEFAULT = "192.168.2.225:2181,192.168.2.225:2182,192.168.2.225:2183";
    private final static String ZK_ONLINE_MOBILE_DEFAULT = "10.64.12.238:2181,10.64.12.238:2182,10.64.12.238:2183,10.64.12.237:2181" +
            ",10.64.12.237:2182,10.64.12.237:2183,10.64.12.236:2181,10.64.12.236:2182,10.64.12.236:2183,10.32.32.176:2191,10.32.32.175:2191" +
            ",10.32.32.174:2191,10.32.32.184:2191,10.32.32.183:2191,10.32.35.217:2191,10.4.36.130:2191,10.4.36.131:2191";

    public static String getMonitorZkAddress(String userZkAddress) {
        if(!StringUtils.isEmpty(userZkAddress)) {
            if(userZkAddress.contains("192.168.2.225:2181")) return userZkAddress;
            else if(userZkAddress.contains("10.64.12.238:2181")) return userZkAddress;
        }
        String localIp = ProcessInfoUtil.getLocalIpV4();
        if(localIp.startsWith("10.")) {
            return ZK_ONLINE_MOBILE_DEFAULT;
        }else {
            return ZK_OFFLINE_MOBILE_DEFAULT;
        }
    }
}
