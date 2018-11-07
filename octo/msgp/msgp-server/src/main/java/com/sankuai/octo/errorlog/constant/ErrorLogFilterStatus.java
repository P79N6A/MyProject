package com.sankuai.octo.errorlog.constant;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-11-26
 */
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
