package com.sankuai.octo.msgp.domain;

public class IdcIpprefix {
    private String idc;
    private String ipprefix;

    public IdcIpprefix(){

    }
    public IdcIpprefix(String idc,String ipprefix){
        this.idc = idc;
        this.ipprefix = ipprefix;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public String getIpprefix() {
        return ipprefix;
    }

    public void setIpprefix(String ipprefix) {
        this.ipprefix = ipprefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdcIpprefix hostIp = (IdcIpprefix) o;

        if (!idc.equals(hostIp.idc)) return false;
        return ipprefix.equals(hostIp.ipprefix);

    }

    @Override
    public int hashCode() {
        int result = idc.hashCode();
        result = 31 * result + ipprefix.hashCode();
        return result;
    }
}
