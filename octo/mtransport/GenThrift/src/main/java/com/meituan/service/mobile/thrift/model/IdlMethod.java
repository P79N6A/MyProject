package com.meituan.service.mobile.thrift.model;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gaosheng
 * Date: 15-1-21
 * Time: 下午4:40
 * To change this template use File | Settings | File Templates.
 */
public class IdlMethod {

    private String name;
    private String type;
    private List<IdlParameter> parameterList;


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

    public List<IdlParameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<IdlParameter> parameterList) {
        this.parameterList = parameterList;
    }
}
