package com.sankuai.octo.msgp.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

public class CapacityResult {
    private boolean isError;
    private String message;
    private List<CapacityData> data;

    public CapacityResult() {
    }

    public boolean isError() {
        return isError;
    }

    public void setIsError(boolean isError) {
        this.isError = isError;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<CapacityData> getData() {
        return data;
    }

    public void setData(List<CapacityData> data) {
        this.data = data;
    }


    public String toString(){
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
