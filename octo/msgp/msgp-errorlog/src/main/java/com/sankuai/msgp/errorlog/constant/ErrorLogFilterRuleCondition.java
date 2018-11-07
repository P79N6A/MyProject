package com.sankuai.msgp.errorlog.constant;

public enum ErrorLogFilterRuleCondition {
    ANYOF("anyof", 0),
    ALLOF("allof", 1);

    private String value;
    private Integer index;

    private ErrorLogFilterRuleCondition(String value, Integer index) {
        this.value = value;
        this.index = index;
    }

    public String getValue() {
        return value;
    }

    public Integer getIndex() {
        return index;
    }
}
