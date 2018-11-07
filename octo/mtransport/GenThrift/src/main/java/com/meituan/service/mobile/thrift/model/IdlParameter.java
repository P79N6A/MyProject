package com.meituan.service.mobile.thrift.model;

/**
 * Created with IntelliJ IDEA.
 * User: gaosheng
 * Date: 15-1-21
 * Time: 下午4:45
 * To change this template use File | Settings | File Templates.
 */
public class IdlParameter {
    private String name;
    private String type;

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
}
