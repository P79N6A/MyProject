package com.sankuai.msgp.errorlog.domain;

public class Result {
    private Object data;
    private boolean successed = true;

    public Result() {
    }

    public Result(Object data){
        this.data = data;
    }

    public Result(Object data,boolean successed){
        this.data = data;
        this.successed = successed;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isSuccessed() {
        return successed;
    }

    public void setSuccessed(boolean successed) {
        this.successed = successed;
    }
}
