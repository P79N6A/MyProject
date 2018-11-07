package com.sankuai.mtthrift.testSuite.httpinvoke;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ImplClass extends AbstractClass implements InterfaceClass {

    private String str;

    public ImplClass() {
        name = "Emma";
        str = "hah";
    }
    @Override
    public void eat() {

    }

    @Override
    public void run() {

    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
