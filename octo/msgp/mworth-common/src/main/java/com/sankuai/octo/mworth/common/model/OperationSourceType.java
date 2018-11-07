package com.sankuai.octo.mworth.common.model;

/**
 * Created by zava on 15/12/7.
 */
public enum OperationSourceType {
    HUMAN(0, "人"),
    SYSTEM(1, "系统");

    private int type;
    private String name;

    OperationSourceType(int type, String name) {
        this.name = name;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
