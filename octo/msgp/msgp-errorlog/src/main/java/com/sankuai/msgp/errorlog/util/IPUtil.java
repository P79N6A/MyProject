package com.sankuai.msgp.errorlog.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by emma on 2017/9/27.
 */
public class IPUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(IPUtil.class);

    public static String getHostnameByIP(String ip) {
        String hostname = null;
        String wholeHostname = null;
        try {
            wholeHostname = InetAddress.getByAddress(toIpByte(ip)).getHostName();
        } catch (UnknownHostException e) {
            LOGGER.error("Get hostname from ip fail.", e);
        }
        String[] tempStr = wholeHostname.split("\\.");
        if (tempStr.length > 0) {
            hostname = tempStr[0];
        }
        return hostname;
    }

    private static byte[] toIpByte(String ip) {
        String[] ips=ip.split("\\.");
        byte[] address=new byte[ips.length];
        for (int i = 0; i < ips.length; i++)
        {
            address[i]=(byte) Integer.parseInt(ips[i]);
        }
        return address;
    }
}
