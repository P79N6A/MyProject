package com.meituan.service.mobile.thrift.model;

/**
 * Created with IntelliJ IDEA.
 * User: gaosheng
 * Date: 15-1-21
 * Time: 下午3:13
 * To change this template use File | Settings | File Templates.
 */
public class IdlVariable {

    private String name;
    private String type;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
