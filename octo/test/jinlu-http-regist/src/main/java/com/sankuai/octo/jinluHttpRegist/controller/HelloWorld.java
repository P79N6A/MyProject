package com.sankuai.octo.jinluHttpRegist.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by zhangjinlu on 15/8/23.
 */

@Controller
public class HelloWorld {
    private final Logger logger = LoggerFactory.getLogger(HelloWorld.class);

    @RequestMapping("/welcome")
    public ModelAndView helloWorld() {
        logger.warn("【In HelloWorld.helloWorld】");
        String localIP = getLocalIpV4(null);
        String message = "<br><div style='text-align:center;'><h3>********** "
                         +localIP+ " **********</div><br><br>";
        return new ModelAndView("welcome", "message", message);
    }

    public static String getLocalIpV4(String tryIp) {
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
                    if (isBlank(ip)) {
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

        if (isBlank(ip)) {
            throw new RuntimeException("can not find local ip!");
        }

        return ip;
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
}
