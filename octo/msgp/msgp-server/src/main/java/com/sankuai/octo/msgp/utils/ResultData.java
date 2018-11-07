package com.sankuai.octo.msgp.utils;

/**
 * Created by emma on 2017/5/26.
 */
public class ResultData<T> {

    private boolean isSuccess = false;
    private String msg;
    private T data;

    public ResultData() {
    }

    public ResultData(boolean isSuccess, T data) {
        this.isSuccess = isSuccess;
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setSuccResult(T data) {
        this.data = data;
        this.isSuccess = true;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * 成功
     */
    public ResultData<T> success(T data) {
        this.isSuccess = true;
        this.data = data;
        return this;
    }

    /**
     * 失败
     */
    public ResultData<T> failure(String msg) {
        this.isSuccess = false;
        this.msg = msg;
        return this;
    }

    @Override
    public String toString() {
        String dataStr = data != null ? data.toString() : null;
        return "[" + isSuccess + ", " + dataStr + "]";
    }
}
