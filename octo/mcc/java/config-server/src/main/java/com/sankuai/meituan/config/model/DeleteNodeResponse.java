package com.sankuai.meituan.config.model;

/**
 * Created by liangchen on 2017/11/27.
 */
public class DeleteNodeResponse {
    private boolean ret;

    private String msg;

    private boolean existChild;

    public boolean isRet() {
        return ret;
    }

    public void setRet(boolean ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isExistChild() {
        return existChild;
    }

    public void setExistChild(boolean existChild) {
        this.existChild = existChild;
    }
}
