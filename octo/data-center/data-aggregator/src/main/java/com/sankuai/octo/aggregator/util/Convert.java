package com.sankuai.octo.aggregator.util;

public class Convert {
    public Convert() {
    }

    public static int ipToInt(String ip) {
        if(ip == null) {
            return 0;
        } else {
            int i = ip.lastIndexOf("//");
            if(i >= 0) {
                ip = ip.substring(i + 2);
            }

            String[] items = ip.split("\\.");

            try {
                return Integer.valueOf(items[0]).intValue() << 24 | Integer.valueOf(items[1]).intValue() << 16 | Integer.valueOf(items[2]).intValue() << 8 | Integer.valueOf(items[3]).intValue();
            } catch (NumberFormatException var4) {
                return 0;
            }
        }
    }

    public static String intToIp(int ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(ip >>> 24));
        sb.append(".");
        sb.append(String.valueOf((ip & 16777215) >>> 16));
        sb.append(".");
        sb.append(String.valueOf((ip & '\uffff') >>> 8));
        sb.append(".");
        sb.append(String.valueOf(ip & 255));
        return sb.toString();
    }
}
