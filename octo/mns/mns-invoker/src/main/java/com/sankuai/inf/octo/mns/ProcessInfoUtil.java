package com.sankuai.inf.octo.mns;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

@Deprecated
public class ProcessInfoUtil {
    private ProcessInfoUtil() {

    }

    public static String getLocalIpV4() {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4();
    }

    public static boolean isLocalHostOnline() {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.isLocalHostOnline();
    }

    public static boolean isOnlineHost(String ip) {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.isOnlineHost(ip);
    }

    public static String getHostInfoByIp(String ip) {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getHostInfoByIp(ip);
    }

    public static String getHostNameInfoByIp() {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getHostNameInfoByIp();
    }

    public static boolean isPigeonEnvOnline() {
       return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.isPigeonEnvOnline();
    }

    // xml格式有问题, 解析失败; 所以用字符串方式处理.
    public static String getOctoEnv() {
       return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getOctoEnv();
    }

    public static Properties loadFromXmlFile() throws IOException {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.loadFromXmlFile();
    }

    public static String getHostName() {
       return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getHostName();
    }

    public static String getHostName(String ip) {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getHostName(ip);
    }

    public static int getPid() {
       return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getPid();
    }

    public static String getIpPid() {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getIpPid();
    }

    public static List<String> serviceGroupingByDCLocation(
            List<String> servers) {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.serviceGroupingByDCLocation(servers);
    }

    public static List<String> serviceGroupingByDCLocation(List<String> servers,
                                                           boolean enableRemoteServer) {
       return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.serviceGroupingByDCLocation(servers,enableRemoteServer);
    }

    public static boolean isSameDC(String remoteIp) {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.isSameDC(remoteIp);
    }

    public static String getLocalIpV4(String ip) {
        return com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4(ip);
    }

}