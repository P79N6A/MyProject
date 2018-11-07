package com.sankuai.octo.statistic.model;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.util.List;
import java.util.Map;

@ThriftStruct
public final class QpsResult {
    private QpsRetrieveCode code;
    private Map<String, List<QpsLog>> qpsLogMap;

    @ThriftConstructor
    public QpsResult(QpsRetrieveCode code, Map<String, List<QpsLog>> qpsLogMap) {
        this.code = code;
        this.qpsLogMap = qpsLogMap;
    }

    @ThriftField(1)
    public QpsRetrieveCode getCode() {
        return code;
    }

    @ThriftField
    public void setCode(QpsRetrieveCode code) {
        this.code = code;
    }

    @ThriftField(2)
    public Map<String, List<QpsLog>> getQpsLogMap() {
        return qpsLogMap;
    }

    @ThriftField
    public void setQpsLogMap(Map<String, List<QpsLog>> qpsLogMap) {
        this.qpsLogMap = qpsLogMap;
    }

    @Override
    public String toString() {
        return "QpsResult{" +
                "code=" + code +
                ", qpsLogMap=" + qpsLogMap +
                '}';
    }
}
