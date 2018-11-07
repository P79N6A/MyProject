package com.sankuai.octo.statistic.model;

import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftEnumValue;

@ThriftEnum
public enum StatSource {
    Server(0),
    ServerSlow(1),
    ServerDrop(2),
    Client(0),
    ClientSlow(1),
    ClientDrop(2),
    RemoteClient(0),
    RemoteClientSlow(1),
    RemoteClientDrop(2),
    ServerFailure(3),
    ClientFailure(3),
    RemoteClientFailure(3);

    private int dataType;

    StatSource(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }

    @ThriftEnumValue
    public int getIntValue() {
        return this.ordinal();
    }
}
