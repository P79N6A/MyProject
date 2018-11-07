package com.sankuai.octo.msgp.domain;

import java.util.List;

/**
 * @author uu
 * @description
 * @date Created in 17:32 2018/5/15
 * @modified
 */
public class SreUserData {
    private List<SreUser> data;
    private Exception error;

    public List<SreUser> getData() {
        return data;
    }

    public void setData(List<SreUser> data) {
        this.data = data;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }
}
