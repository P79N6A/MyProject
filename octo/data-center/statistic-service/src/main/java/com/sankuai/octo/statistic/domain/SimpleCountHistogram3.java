package com.sankuai.octo.statistic.domain;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;


@ThriftStruct
public class SimpleCountHistogram3 {
    @ThriftField(1)
    public long count;
    @ThriftField(2)
    public long successCount;
    @ThriftField(3)
    public long exceptionCount;
    @ThriftField(4)
    public long timeoutCount;
    @ThriftField(5)
    public long dropCount;
    @ThriftField(6)
    public long HTTP2XXCount;
    @ThriftField(6)
    public long HTTP3XXCount;
    @ThriftField(8)
    public long HTTP4XXCount;
    @ThriftField(9)
    public long HTTP5XXCount;

    @ThriftField(10)
    public long version;
    @ThriftField(11)
    public long createTime;
    @ThriftField(12)
    public long updateTime;

    @ThriftField(13)
    public SimpleCountReservoir3 reservoir;



    @ThriftConstructor
    public SimpleCountHistogram3(long count,long successCount,long exceptionCount,long timeoutCount,long dropCount,
                                 long HTTP2XXCount,long HTTP3XXCount,long HTTP4XXCount,long HTTP5XXCount,
                                 long version,long createTime,long updateTime,
                                 SimpleCountReservoir3 reservoir) {

        this.count = count;
        this.successCount = successCount;
        this.exceptionCount = exceptionCount;
        this.timeoutCount = timeoutCount;
        this.dropCount = dropCount;
        this.HTTP2XXCount = HTTP2XXCount;
        this.HTTP3XXCount = HTTP3XXCount;
        this.HTTP4XXCount = HTTP4XXCount;
        this.HTTP5XXCount = HTTP5XXCount;
        this.version = version;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.reservoir = reservoir;
    }

    public long getCount() {
        return count;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getExceptionCount() {
        return exceptionCount;
    }

    public long getTimeoutCount() {
        return timeoutCount;
    }

    public long getDropCount() {
        return dropCount;
    }

    public long getHTTP2XXCount() {
        return HTTP2XXCount;
    }

    public long getHTTP3XXCount() {
        return HTTP3XXCount;
    }

    public long getHTTP4XXCount() {
        return HTTP4XXCount;
    }

    public long getHTTP5XXCount() {
        return HTTP5XXCount;
    }

    public long getVersion() {
        return version;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public SimpleCountReservoir3 getReservoir() {
        return reservoir;
    }
}
