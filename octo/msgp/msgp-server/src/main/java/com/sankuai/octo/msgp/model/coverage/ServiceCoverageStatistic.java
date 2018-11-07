package com.sankuai.octo.msgp.model.coverage;

import java.sql.Timestamp;

public class ServiceCoverageStatistic {
    private String statdate;
    private int base;
    private String owt;

    private int http;
    private int java;

    private int mtthrift;
    private int hlb;
    private int mns;
    private int mcc;
    private int hulk;
    private int mtrace;
    private int xmdlog;
    private int infbom;
    private int ptest;
    private int serviceDegrade;

    private Timestamp createTime;
    private Timestamp updateTime;

    public String getStatdate() {
        return statdate;
    }

    public void setStatdate(String statdate) {
        this.statdate = statdate;
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

    public int getHttp() {
        return http;
    }

    public void setHttp(int http) {
        this.http = http;
    }

    public int getJava() {
        return java;
    }

    public void setJava(int java) {
        this.java = java;
    }

    public int getMtthrift() {
        return mtthrift;
    }

    public void setMtthrift(int mtthrift) {
        this.mtthrift = mtthrift;
    }

    public int getHlb() {
        return hlb;
    }

    public void setHlb(int hlb) {
        this.hlb = hlb;
    }

    public int getMns() {
        return mns;
    }

    public void setMns(int mns) {
        this.mns = mns;
    }

    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getHulk() {
        return hulk;
    }

    public void setHulk(int hulk) {
        this.hulk = hulk;
    }

    public int getMtrace() {
        return mtrace;
    }

    public void setMtrace(int mtrace) {
        this.mtrace = mtrace;
    }

    public int getXmdlog() {
        return xmdlog;
    }

    public void setXmdlog(int xmdlog) {
        this.xmdlog = xmdlog;
    }

    public int getInfbom() {
        return infbom;
    }

    public void setInfbom(int infbom) {
        this.infbom = infbom;
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

    public int getPtest() {
        return ptest;
    }

    public void setPtest(int ptest) {
        this.ptest = ptest;
    }

    public int getServiceDegrade() {
        return serviceDegrade;
    }

    public void setServiceDegrade(int serviceDegrade) {
        this.serviceDegrade = serviceDegrade;
    }
}
