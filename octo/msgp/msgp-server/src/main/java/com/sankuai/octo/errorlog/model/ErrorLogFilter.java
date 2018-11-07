package com.sankuai.octo.errorlog.model;

import java.util.Date;

public class ErrorLogFilter {
    private Integer id;

    private String name;

    private String appkey;

    private Boolean terminate;

    private Long sortNum;

    private String rules;

    private Integer ruleCondition;

    private Boolean enabled;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Integer operatorId;

    private Boolean alarm;

    private Integer threhold;

    private Integer thresholdMin;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey == null ? null : appkey.trim();
    }

    public Boolean getTerminate() {
        return terminate;
    }

    public void setTerminate(Boolean terminate) {
        this.terminate = terminate;
    }

    public Long getSortNum() {
        return sortNum;
    }

    public void setSortNum(Long sortNum) {
        this.sortNum = sortNum;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules == null ? null : rules.trim();
    }

    public Integer getRuleCondition() {
        return ruleCondition;
    }

    public void setRuleCondition(Integer ruleCondition) {
        this.ruleCondition = ruleCondition;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public Boolean getAlarm() {
        return alarm;
    }

    public void setAlarm(Boolean alarm) {
        this.alarm = alarm;
    }

    public Integer getThrehold() {
        return threhold;
    }

    public void setThrehold(Integer threhold) {
        this.threhold = threhold;
    }


    public Integer getThresholdMin() {
        return thresholdMin;
    }

    public void setThresholdMin(Integer thresholdMin) {
        this.thresholdMin = thresholdMin;
    }
}