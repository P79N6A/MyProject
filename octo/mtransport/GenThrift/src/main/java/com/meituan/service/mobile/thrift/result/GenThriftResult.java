package com.meituan.service.mobile.thrift.result;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-1-30
 * Time: 下午7:50
 */
@Component
@Scope("prototype")
public class GenThriftResult {

    private GenThriftFlag flag;
    private String error;

    public GenThriftFlag getFlag() {
        return flag;
    }

    public void setFlag(GenThriftFlag flag) {
        this.flag = flag;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
