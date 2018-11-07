package com.sankuai.meituan.config.model;

public class EmbededNode {

    private String name;
    private boolean isLeaf;

    public EmbededNode(String name, boolean isLeaf){
        this.name = name;
        this.isLeaf = isLeaf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }
}
