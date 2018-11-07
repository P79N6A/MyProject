package com.sankuai.octo.mns;

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;

public class ProcessInfoUtilTest {

    public static void main(String[] args) {
//        String hostinfo = ProcessInfoUtil.getHostInfoByIp("10.20.50.34");
//        System.out.println(hostinfo);
        String ips = "10.20.50.13;10.20.61.2;10.20.50.18;10.20.50.235;10.20.50.33;10.20.50.34;10.20.50.35;10.20.50.51;10.20.60.212;10.20.61.146;10.20.61.148;10.20.61.210;10.4.233.156;10.4.237.168;10.4.241.35;10.4.243.141;10.4.243.201;10.4.247.137;172.16.16.173;172.16.17.239;172.16.18.205;172.16.19.35;172.18.113.32;172.18.163.196;172.18.163.212;172.18.164.3;172.18.165.148;172.18.168.160;172.18.169.6;172.18.172.134;172.18.173.67;172.18.174.203;172.18.174.4;172.18.175.148;172.18.175.58;172.18.176.91;172.18.177.64;172.18.181.116;172.18.183.190;172.18.184.200;172.18.185.146;172.18.185.196;172.18.185.233;172.18.202.160;172.18.40.237;172.18.40.247;172.21.143.119;192.168.168.106;192.168.2.1";
        String[] ip_arr = ips.split(";");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 2; i++) {
            for (String ip : ip_arr) {
                String hostname = ProcessInfoUtil.getHostName(ip);
                System.out.println(hostname);
            }
        }
        System.out.println("time:" + (System.currentTimeMillis() - start) + "ms");
    }
}
