package com.sankuai.octo.errorlog.constant;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-10-17
 */
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
