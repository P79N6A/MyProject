package com.meituan.service.mobile.mtthrift.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class NetUtil {
    public static final String ANYHOST = "0.0.0.0";

    public static int getAvailablePort(int defaultPort) {
        int port = defaultPort;
        while (port < 65535) {
            if (!isPortInUse(port)) {
                return port;
            } else {
                port++;
            }
        }
        while (port > 0) {
            if (!isPortInUse(port)) {
                return port;
            } else {
                port--;
            }
        }
        throw new IllegalStateException("No available port");
    }

    public static String toIpPort(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    private static boolean isPortInUse(int port) {
        try {
            bindPort(ANYHOST, port);
            bindPort(InetAddress.getLocalHost().getHostAddress(), port);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private static void bindPort(String host, int port) throws IOException {
        ServerSocket s = new ServerSocket();
        s.bind(new InetSocketAddress(host, port));
        s.close();
    }
}
