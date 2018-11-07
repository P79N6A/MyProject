package com.sankuai.meituan.config.model;

/**
 * Created by liangchen on 2017/12/8.
 */
public class WrappedEmbededNode extends EmbededNode {
    protected boolean enableAdd;

    public WrappedEmbededNode(String name, boolean isLeaf, boolean enableAdd) {
        super(name, isLeaf);
        this.enableAdd = enableAdd;
    }

    public boolean isEnableAdd() {
        return enableAdd;
    }

    public void setEnableAdd(boolean enableAdd) {
        this.enableAdd = enableAdd;
    }
}
