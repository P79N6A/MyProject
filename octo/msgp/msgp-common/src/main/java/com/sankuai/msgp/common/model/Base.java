package com.sankuai.msgp.common.model;

/**
 * Created by yves on 17/1/4.
 */
public enum Base {
    meituan(0, "meituan"), dianping(1, "dianping"), all(2, "all");

    private Base(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Base{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    private int id;
    private String name;
}
