package com.sankuai.meituan.config.constant;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-6
 */
public enum UserStatus {
    ACTIVE(0),
    DELETE(128);

    private Integer index;

    private UserStatus(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }
}
