package com.sankuai.octo.plugins.enforcer;

import java.io.*;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Util {

    private Util() {
    }

    public static String getLocalIpV4() {
        Enumeration<NetworkInterface> networkInterface;
        try {
            networkInterface = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
        String ip = "";
        Set<String> ips = new HashSet<String>();
        while (networkInterface.hasMoreElements()) {
            NetworkInterface ni = networkInterface.nextElement();
            // 忽略虚拟网卡的IP,docker 容器的IP
            if (ni.getName().contains("vnic") || ni.getName().contains("docker")
                    || ni.getName().contains("vmnet") || ni.getName().contains("vmbox")) {
                continue;
            }

            Enumeration<InetAddress> inetAddress = ni.getInetAddresses();
            while (inetAddress.hasMoreElements()) {
                InetAddress ia = inetAddress.nextElement();
                if (ia instanceof Inet6Address) {
                    continue; // ignore ipv6
                }
                String thisIp = ia.getHostAddress();
                // 排除 回送地址
                if (!ia.isLoopbackAddress() && !thisIp.contains(":") && !"127.0.0.1".equals(thisIp)) {
                    ips.add(thisIp);
                    if (ip == null || ip.trim().isEmpty()) {
                        ip = thisIp;
                    }
                }
            }
        }

        // 为新办公云主机绑定了两个IP, 只用其 10 段IP
        if (ips.size() >= 2) {
            for (String str : ips) {
                if (str.startsWith("10.")) {
                    ip = str;
                    break;
                }
            }
        }

        if (ip == null || ip.trim().isEmpty()) {
            throw new RuntimeException("can not find local ip!");
        }

        return ip;
    }

    public static boolean isLocalHostOnline() {
        return isOnlineHost(getLocalIpV4());
    }

    public static boolean isOnlineHost(String ip) {
        boolean online = false;
        String host = getHostInfoByIp(ip);
        if (host.contains(".office.mos") || host.contains(".corp.sankuai.com")) {
            online = false;
        } else if (host.contains(".sankuai.com")) {
            online = true;
        } else if (isPigeonEnvOnline()) {
            online = true;
        } else {
            online = false;
        }

        return online;
    }

    public static String getHostInfoByIp(String ip) {
        return getSystemInfoByCommand(new String[]{"host", ip});
    }

    public static String getHostNameInfoByIp() {
        return getSystemInfoByCommand(new String[]{"hostname"});
    }

    private static String getSystemInfoByCommand(String[] command) {
        String result = "";
        BufferedReader read = null;
        InputStream in = null;
        Process pro = null;
        try {
            pro = Runtime.getRuntime().exec(command);
            pro.waitFor();
            in = pro.getInputStream();
            read = new BufferedReader(new InputStreamReader(in));
            result = read.readLine();
        } catch (Exception e) {
        } finally {
            try {
                if (null != pro) {
                    pro.destroy();
                }
                if (null != read) {
                    read.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static final String PIGEON_ENV_FILE = "/data/webapps/appenv";
    public static final String PIGEON_ENV_KEY = "deployenv";
    public static final String PIGEON_ENV_VALUE_ONLINE = "product";

    public static boolean isPigeonEnvOnline() {
        boolean online = false;
        try {
            Properties props = loadFromFile();
            for (String key : props.stringPropertyNames()) {
                if (key.equals(PIGEON_ENV_KEY)) {
                    String value = props.getProperty(key);
                    online = PIGEON_ENV_VALUE_ONLINE.equalsIgnoreCase(value.trim());
                    break;
                }
            }
        } catch (IOException e) {
        }
        return online;
    }

    private static Properties loadFromFile() throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(PIGEON_ENV_FILE);
            props.load(in);
        } catch (FileNotFoundException e) {
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return props;
    }
}