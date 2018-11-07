package com.sankuai.octo.sgnotify.comm;

public enum FileConfigCmdType {
    DISTRIBUTE(0),
    ENABLE(1);

    private int value;

    FileConfigCmdType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
