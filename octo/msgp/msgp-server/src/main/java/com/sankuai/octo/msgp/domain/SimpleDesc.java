package com.sankuai.octo.msgp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * Created by zava on 16/9/21.
 */
public class SimpleDesc {
    private String appkey;
    private List<String> owners;
    private List<String> observers;
    private int base = 0;
    private int regLimit = 0;
    private String owt;
    private String pdl;
    private String owt_cn ="";
    private String pdl_cn="";
    private String tags;
    private String intro;

    public SimpleDesc() {

    }

    public SimpleDesc(String appkey, List<String> owners, List<String> observers,
                      int base, String owt, String pdl, String tags, String intro) {
        this.appkey = appkey;
        this.owners = owners;
        this.observers = observers;
        this.base = base;
        this.owt = owt;
        this.pdl = pdl;
        this.tags = tags;
        this.intro = intro;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public List<String> getObservers() {
        return observers;
    }

    public void setObservers(List<String> observers) {
        this.observers = observers;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
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

    public String getPdl() {
        return pdl;
    }

    public void setPdl(String pdl) {
        this.pdl = pdl;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getIntro() {
        return intro;
    }

    public int getRegLimit() {
        return regLimit;
    }

    public void setRegLimit(int regLimit) {
        this.regLimit = regLimit;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getOwt_cn() {
        return owt_cn;
    }

    public void setOwt_cn(String owt_cn) {
        this.owt_cn = owt_cn;
    }

    public String getPdl_cn() {
        return pdl_cn;
    }

    public void setPdl_cn(String pdl_cn) {
        this.pdl_cn = pdl_cn;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
