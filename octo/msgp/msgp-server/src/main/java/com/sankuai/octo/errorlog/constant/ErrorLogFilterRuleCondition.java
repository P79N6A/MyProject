package com.sankuai.octo.errorlog.constant;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-11-25
 */
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
