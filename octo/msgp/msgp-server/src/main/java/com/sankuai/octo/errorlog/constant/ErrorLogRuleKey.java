package com.sankuai.octo.errorlog.constant;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-11-26
 */
public enum ErrorLogRuleKey {
    MESSAGE("message"),
    EXCEPTION("exception");

    private String value;

    ErrorLogRuleKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
