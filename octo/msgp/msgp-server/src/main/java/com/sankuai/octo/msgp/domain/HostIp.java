package com.sankuai.octo.msgp.domain;

/**
 * Created by wujinwu on 16/5/11.
 */
public class HostIp {

    private String hostname;

    private String ip;

    public HostIp() {
    }

    public HostIp(String hostname, String ip) {
        this.hostname = hostname;
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HostIp hostIp = (HostIp) o;

        if (!hostname.equals(hostIp.hostname)) return false;
        return ip.equals(hostIp.ip);

    }

    @Override
    public int hashCode() {
        int result = hostname.hashCode();
        result = 31 * result + ip.hashCode();
        return result;
    }
}
