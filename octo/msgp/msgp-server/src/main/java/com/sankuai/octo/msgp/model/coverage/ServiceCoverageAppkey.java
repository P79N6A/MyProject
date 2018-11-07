package com.sankuai.octo.msgp.model.coverage;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.sql.Timestamp;

public class ServiceCoverageAppkey {
    private String statdate;
    private String appkey;
    private int base;
    private String owt;

    private boolean http;
    private boolean java;

    private boolean mtthrift;
    private boolean hlb;
    private boolean mns;
    private boolean mcc;
    private boolean hulk;
    private boolean mtrace;
    private boolean xmdlog;
    private boolean infbom;
    private boolean ptest;
    private boolean serviceDegrade;

    private Timestamp createTime;
    private Timestamp updateTime;

    public ServiceCoverageAppkey(String statdate, String appkey, int base, String owt) {
        this.statdate = statdate;
        this.appkey = appkey;
        this.base = base;
        this.owt = owt;
        this.createTime = new Timestamp(System.currentTimeMillis());
    }

    public String getStatdate() {
        return statdate;
    }

    public void setStatdate(String statdate) {
        this.statdate = statdate;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public String getOwt() {
        return owt;
    }

    public void setOwt(String owt) {
        this.owt = owt;
    }

    public boolean getHttp() {
        return http;
    }

    public void setHttp(boolean http) {
        this.http = http;
    }

    public boolean getJava() {
        return java;
    }

    public void setJava(boolean java) {
        this.java = java;
    }

    public boolean getMtthrift() {
        return mtthrift;
    }

    public void setMtthrift(boolean mtthrift) {
        this.mtthrift = mtthrift;
    }

    public boolean getHlb() {
        return hlb;
    }

    public void setHlb(boolean hlb) {
        this.hlb = hlb;
    }

    public boolean getMns() {
        return mns;
    }

    public void setMns(boolean mns) {
        this.mns = mns;
    }

    public boolean getMcc() {
        return mcc;
    }

    public void setMcc(boolean mcc) {
        this.mcc = mcc;
    }

    public boolean getHulk() {
        return hulk;
    }

    public void setHulk(boolean hulk) {
        this.hulk = hulk;
    }

    public boolean getMtrace() {
        return mtrace;
    }

    public void setMtrace(boolean mtrace) {
        this.mtrace = mtrace;
    }

    public boolean getXmdlog() {
        return xmdlog;
    }

    public void setXmdlog(boolean xmdlog) {
        this.xmdlog = xmdlog;
    }

    public boolean getInfbom() {
        return infbom;
    }

    public void setInfbom(boolean infbom) {
        this.infbom = infbom;
    }

    public boolean getPtest() {
        return ptest;
    }

    public void setPtest(boolean ptest) {
        this.ptest = ptest;
    }

    public boolean getServiceDegrade() {
        return serviceDegrade;
    }

    public void setServiceDegrade(boolean serviceDegrade) {
        this.serviceDegrade = serviceDegrade;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
