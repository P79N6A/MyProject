package com.meituan.dorado.rpc.meta;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/5/21
 */
public class RpcResult {
    private Object returnVal;

    public RpcResult(){}

    public RpcResult(Object returnVal) {
        this.returnVal = returnVal;
    }

    public Object getReturnVal() {
        return returnVal;
    }

    public void setReturnVal(Object returnVal) {
        this.returnVal = returnVal;
    }
}
