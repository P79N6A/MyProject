package com.meituan.service.mobile.thrift.model;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gaosheng
 * Date: 15-1-21
 * Time: 下午3:11
 * To change this template use File | Settings | File Templates.
 */
public class IdlStruct {
    private String structName;
    private List<IdlVariable> variableList;

    public String getStructName() {
        return structName;
    }

    public void setStructName(String structName) {
        this.structName = structName;
    }

    public List<IdlVariable> getVariableList() {
        return variableList;
    }

    public void setVariableList(List<IdlVariable> variableList) {
        this.variableList = variableList;
    }
}
