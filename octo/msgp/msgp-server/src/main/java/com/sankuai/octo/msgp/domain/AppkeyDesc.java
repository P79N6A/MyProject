package com.sankuai.octo.msgp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

public class AppkeyDesc {
    private String name;
    private String appkey;
    private String baseApp;
    private List<DescUser> owners;
    private List<DescUser> observers = new ArrayList();
    private String intro="";
    private String category="";
    private int business = 100;
    private String group = "";
    private int base = 0;
    private String owt = "";
    private String pdl = "";
    private int level = 0;
    private String tags="";
    private int regLimit = 0;
    private Long createTime = System.currentTimeMillis();

    public AppkeyDesc() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getBaseApp() {
        return baseApp;
    }

    public void setBaseApp(String baseApp) {
        this.baseApp = baseApp;
    }

    public List<DescUser> getOwners() {
        return owners;
    }

    public void setOwners(List<DescUser> owners) {
        this.owners = owners;
    }

    public List<DescUser> getObservers() {
        return observers;
    }

    public void setObservers(List<DescUser> observers) {
        this.observers = observers;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getBusiness() {
        return business;
    }

    public void setBusiness(int business) {
        this.business = business;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getRegLimit() {
        return regLimit;
    }

    public void setRegLimit(int regLimit) {
        this.regLimit = regLimit;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
