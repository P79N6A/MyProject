package com.sankuai.octo.statistic.domain;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class StatTag3 {
    @ThriftField(1)
    public String spanname;
    @ThriftField(2)
    public String localHost;
    @ThriftField(3)
    public String remoteHost;
    @ThriftField(4)
    public String remoteAppKey;
    @ThriftField(5)
    public String infraName;

    @ThriftConstructor
    public StatTag3(String spanname, String localHost, String remoteHost, String remoteAppKey, String infraName) {
        this.spanname = spanname;
        this.localHost = localHost;
        this.remoteHost = remoteHost;
        this.remoteAppKey = remoteAppKey;
        this.infraName = infraName;
    }

    public String getSpanname() {
        return spanname;
    }

    public String getLocalHost() {
        return localHost;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public String getInfraName() {
        return infraName;
    }
}
