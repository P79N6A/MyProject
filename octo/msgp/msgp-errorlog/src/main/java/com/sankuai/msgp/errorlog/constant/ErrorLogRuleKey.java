package com.sankuai.msgp.errorlog.constant;

public enum ErrorLogRuleKey {
    MESSAGE("message"),
    EXCEPTION("exception"),
    HOSTNAME("hostname");

    private String value;

    private ErrorLogRuleKey(String value) {
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