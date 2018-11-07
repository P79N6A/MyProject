package com.meituan.mtrace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utils {
    private static int BUFFER = 1024;
    public static final String IP = getLocalIpV4();

    public static String getLocalIpV4() {
        Enumeration<NetworkInterface> networkInterface;
        try {
            networkInterface = NetworkInterface.getNetworkInterfaces();
            String ip = "";
            Set<String> ips = new HashSet<String>();
            while (networkInterface.hasMoreElements()) {
                NetworkInterface ni = networkInterface.nextElement();
                // 忽略虚拟网卡
                if (ni.getName().contains("vnic")) {
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
                    if (!ia.isLoopbackAddress() && !thisIp.contains(":")
                            && !"127.0.0.1".equals(thisIp)) {
                        ips.add(thisIp);
                        if (Validate.isBlank(ip)) {
                            ip = thisIp;
                        }
                    }
                }
            }

            // 为新办公云主机所做的特殊处理 :
            if (ips.size() >= 2) {
                for (String str : ips) {
                    if (str.startsWith("10.")) {
                        ip = str;
                        break;
                    }
                }
            }
            return ip;
        } catch (Exception e) {
            return "";
        }

    }

    public static byte[] compress(byte[] data) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        GZIPOutputStream gos = new GZIPOutputStream(os);

        int count;
        byte temp[] = new byte[BUFFER];
        while ((count = is.read(temp, 0, BUFFER)) != -1) {
            gos.write(temp, 0, count);
        }

        gos.finish();
        gos.flush();
        gos.close();

        byte[] output = os.toByteArray();

        os.flush();
        os.close();

        is.close();

        return output;
    }

    public static byte[] decompress(byte[] data) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(data);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        GZIPInputStream gis = new GZIPInputStream(is);

        int count;
        byte temp[] = new byte[BUFFER];
        while ((count = gis.read(temp, 0, BUFFER)) != -1) {
            os.write(temp, 0, count);
        }

        gis.close();

        data = os.toByteArray();

        os.flush();
        os.close();

        is.close();

        return data;
    }
}
