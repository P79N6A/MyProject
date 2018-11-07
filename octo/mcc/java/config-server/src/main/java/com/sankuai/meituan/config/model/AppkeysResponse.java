package com.sankuai.meituan.config.model;

import java.util.List;

/**
 * Created by zhangcan on 17/6/2.
 */
public class AppkeysResponse {
    private int ret;
    private List<String> data;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
