package com.sankuai.octo.msgp.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class IpPort {
    private String ip;
    private int port;

    public IpPort(){

    }

    public IpPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        return  new HashCodeBuilder(17, 37).
                append(ip).
                append(port).
                toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;
        IpPort e = (IpPort) obj;
        return new EqualsBuilder().
                append(getIp(), e.getIp()).
                append(getPort(), e.getPort()).
                isEquals();
    }
}
