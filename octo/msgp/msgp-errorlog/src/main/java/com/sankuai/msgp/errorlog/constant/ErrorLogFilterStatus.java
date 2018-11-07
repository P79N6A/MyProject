package com.sankuai.msgp.errorlog.constant;

public enum ErrorLogFilterStatus {
    ACTIVE(0),
    DELETE(1);

    private Integer value;

    private ErrorLogFilterStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
