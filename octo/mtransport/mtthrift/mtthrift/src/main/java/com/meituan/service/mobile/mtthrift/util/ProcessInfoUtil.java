package com.meituan.service.mobile.mtthrift.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: YangXuehua
 * Date: 13-5-29
 * Time: 下午2:25
 * 获取当前进程的一些信息：pid,ip
 */
@Deprecated
public class ProcessInfoUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessInfoUtil.class);
    private static String ipV4Cache = null;

    public static void main(String[] args) throws Exception {
        int pid = getPid();
        String ip = getLocalIpV4();
        System.out.println("pid: " + pid + ",ip=" + ip);
        System.in.read(); // block the program so that we can do some probing on
                          // it
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.debug("getHostName failed...", e);
        }
        return null;
    }

    public static int getPid() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            LOG.debug("getPid failed...", e);
            return -1;
        }
    }

    public static String getLocalIpV4() {
        return getLocalIpV4(null);
    }

    public static String getLocalIpV4FromLocalCache() {
        if (ipV4Cache == null)
            ipV4Cache = getLocalIpV4(null);
        return ipV4Cache;
    }

    public static String getLocalIpV4(String tryIp) {
        Enumeration<NetworkInterface> networkInterface;
        try {
            networkInterface = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
        String ip = null;
        while (networkInterface.hasMoreElements()) {
            NetworkInterface ni = networkInterface.nextElement();
            if(ni.getName().contains("vnic"))
                continue;
            Enumeration<InetAddress> inetAddress = ni.getInetAddresses();
            while (inetAddress.hasMoreElements()) {
                InetAddress ia = inetAddress.nextElement();
                if (ia instanceof Inet6Address)
                    continue; // ignore ipv6
                String thisIp = ia.getHostAddress();
                if (!ia.isLoopbackAddress() && !thisIp.contains(":") && !"127.0.0.1".equals(thisIp) && (isIntranetIpv4(thisIp) || ip == null)) {
                    ip = thisIp;
                    if (ip.equals(tryIp))
                        return tryIp;
                }
            }
        }
        return ip;
    }

    /**
     * 内网ip
     * 10.0.0.0~10.255.255.255
     * 172.16.0.0~172.31.255.255
     * 192.168.0.0~192.168.255.255
     * 169.254.0.0~169.254.255.255
     * 
     * @param ip
     * @return
     */
    private static boolean isIntranetIpv4(String ip) {
        if (ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("169.254.") || ip.matches("^172.(1[6-9]]|2|3[0-1])")) {
            return true;
        }
        return false;
    }

    public static String getIpPid() {
        return getLocalIpV4() + ":pid" + getPid();
    }

    public static List<String> serviceGroupingByDCLocation( List<String> servers) {
        return serviceGroupingByDCLocation(servers, true);
    }

    public static List<String> serviceGroupingByDCLocation( List<String> servers, boolean enableRemoteServer) {
        List<String> localDCServers = new ArrayList<String>();
        List<String> remoteDCServers = new ArrayList<String>();


        String prefix = getLocalIpV4Prefix();

        for(String server : servers) {
            String[] address = server.split(":");
            if (address.length != 2)
                continue;

            // 2 == alive.
            // not good for old version, commented
//            if(2 != validateMtthriftServer(address[0], Integer.parseInt(address[1])) )
//                continue;

            if(server.startsWith(prefix))
                localDCServers.add(server);
            else
                remoteDCServers.add(server);
        }

        if(localDCServers.size() > 0)
            return localDCServers;
        else if(!enableRemoteServer)
            return null;
        else if(remoteDCServers.size() > 0)
            return remoteDCServers;
        else
            return servers;
    }

    // thrift server with no fb303 implementation.
    @Deprecated
    public static boolean validateThriftServer(String ip, int port) {
        return true;
    }

    private static String getLocalIpV4Prefix() {
        String localAddress = ProcessInfoUtil.getLocalIpV4();
        String[] ipSegs = localAddress.split("\\.");
        return ipSegs[0] + "." + ipSegs[1] + ".";

    }
    public static boolean isSameDC(String remoteIp) {
        String prefix = getLocalIpV4Prefix();
        return remoteIp.startsWith(prefix);

    }
}