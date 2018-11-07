package com.sankuai.msgp.errorlog.constant;

public enum LogAlarmSeverity {

    DEFAULT("OK"),

    OK("OK"),

    WARNING("WARNING"),

    ERROR("ERROR"),

    DISASTER("DISASTER");


    private final String value;

    private LogAlarmSeverity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
