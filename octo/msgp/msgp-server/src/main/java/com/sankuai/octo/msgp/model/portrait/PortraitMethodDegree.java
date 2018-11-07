package com.sankuai.octo.msgp.model.portrait;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by zmz on 2017/8/3.
 */
public class PortraitMethodDegree implements Serializable {

    private static final long serialVersionUID = 9011284204994366342L;

    private String method;
    private String tag;


    public PortraitMethodDegree(String method, String tag) {
        this.method = method;
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "PortraitMethodDegree{" +
                "method='" + method + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PortraitMethodDegree that = (PortraitMethodDegree) o;

        return new EqualsBuilder()
                .append(method, that.method)
                .append(tag, that.tag)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(method)
                .append(tag)
                .toHashCode();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
