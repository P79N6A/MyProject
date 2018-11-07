package com.meituan.mtrace.common;

import com.meituan.mtrace.Convert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetAddress;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author zhangzhitong
 * @created 10/8/15
 */
public class CommonTest {
    @Test
    public void testIpAddress() {
        String[] ipList = {"thrift://127.0.0.1",
                "255.255.255.255",
                "192.168.3.24",
                "thrift://172.255.24.13",
                "0.0.0.0"};
        String[] result = {"127.0.0.1",
                "255.255.255.255",
                "192.168.3.24",
                "172.255.24.13",
                "0.0.0.0"};

        for (int i = 0; i < ipList.length; ++i) {
            int ip = Convert.ipToInt(ipList[i]);
            assertEquals(Convert.intToIp(ip), result[i]);
        }
    }

    @Test
    @Ignore
    public void testUUID() {
        UUID uuid = UUID.randomUUID();
        System.out.println("UUID length " + uuid.toString().length());

    }
}
