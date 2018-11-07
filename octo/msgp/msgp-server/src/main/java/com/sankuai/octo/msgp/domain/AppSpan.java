package com.sankuai.octo.msgp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by zava on 16/9/13.
 */
public class AppSpan {
    private String appkey;
    private String spanname;

    public AppSpan(){

    }
    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getSpanname() {
        return spanname;
    }

    public void setSpanname(String spanname) {
        this.spanname = spanname;
    }
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
