package com.sankuai.octo.msgp.domain;

/**
 * Created by yves on 16/9/21.
 */
public enum ComponentUsed {

    Unused("未使用该组件", -1),
    OtherVersion("使用其他版本", 1),
    CurrentVersion("使用当前版本", 2),
    Used("已使用该组件", 3);

    private String name;
    private int index;

    ComponentUsed(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(int index) {
        for (ComponentUsed c : ComponentUsed.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
