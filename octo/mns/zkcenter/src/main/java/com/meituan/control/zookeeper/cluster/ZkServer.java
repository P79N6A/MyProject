package com.meituan.control.zookeeper.cluster;

/**
 * User: jinmengzhe
 * Date: 2015-07-15
 */
public class ZkServer implements Comparable<ZkServer> {
    private String ip = "";
    private String port = "";
    private String jmxPort = "";

    public ZkServer(String ip, String port, String jmxPort) {
        this.ip = ip;
        this.port = port;
        this.jmxPort = jmxPort;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(String jmxPort) {
        this.jmxPort = jmxPort;
    }

    @Override
    public int compareTo(ZkServer o) {
        String ss = ip + ":" + port;
        String oss = o.getIp() + ":" + port;
        return ss.compareTo(oss);
    }
}
