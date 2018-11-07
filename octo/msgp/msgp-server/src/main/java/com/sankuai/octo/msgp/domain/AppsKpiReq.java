package com.sankuai.octo.msgp.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

/**
 * Created by zava on 16/9/13.
 */
public class AppsKpiReq {

    private String env ="prod";
    private String source="service";
    private List<AppSpan> spanList;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date start;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date end;

    public AppsKpiReq(){

    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<AppSpan> getSpanList() {
        return spanList;
    }

    public void setSpanList(List<AppSpan> spanList) {
        this.spanList = spanList;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
