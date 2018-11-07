package com.sankuai.octo.aggregator.swift;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class SwiftSGModuleInvokeInfo {
    @ThriftField(1)
    public String traceId;
    @ThriftField(2)
    public String spanId;
    @ThriftField(3)
    public String spanName;
    @ThriftField(4)
    public String localAppKey;
    @ThriftField(5)
    public String localHost;
    @ThriftField(6)
    public int localPort;
    @ThriftField(7)
    public String remoteAppKey;
    @ThriftField(8)
    public String remoteHost;
    @ThriftField(9)
    public int remotePort;
    @ThriftField(10)
    public long start;
    @ThriftField(11)
    public int cost;
    @ThriftField(12)
    public int type;
    @ThriftField(13)
    public int status;
    @ThriftField(14)
    public int count;
    @ThriftField(15)
    public int debug;
    @ThriftField(16)
    public String extend;

    @ThriftConstructor
    public SwiftSGModuleInvokeInfo(String traceId, String spanId, String spanName, String localAppKey, String localHost, int localPort, String remoteAppKey, String remoteHost, int remotePort, long start, int cost, int type, int status, int count, int debug, String extend) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.spanName = spanName;
        this.localAppKey = localAppKey;
        this.localHost = localHost;
        this.localPort = localPort;
        this.remoteAppKey = remoteAppKey;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.start = start;
        this.cost = cost;
        this.type = type;
        this.status = status;
        this.count = count;
        this.debug = debug;
        this.extend = extend;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwiftSGModuleInvokeInfo)) return false;

        SwiftSGModuleInvokeInfo that = (SwiftSGModuleInvokeInfo) o;

        if (cost != that.cost) return false;
        if (count != that.count) return false;
        if (debug != that.debug) return false;
        if (localPort != that.localPort) return false;
        if (remotePort != that.remotePort) return false;
        if (start != that.start) return false;
        if (status != that.status) return false;
        if (type != that.type) return false;
        if (extend != null ? !extend.equals(that.extend) : that.extend != null) return false;
        if (localAppKey != null ? !localAppKey.equals(that.localAppKey) : that.localAppKey != null) return false;
        if (localHost != null ? !localHost.equals(that.localHost) : that.localHost != null) return false;
        if (remoteAppKey != null ? !remoteAppKey.equals(that.remoteAppKey) : that.remoteAppKey != null) return false;
        if (remoteHost != null ? !remoteHost.equals(that.remoteHost) : that.remoteHost != null) return false;
        if (spanId != null ? !spanId.equals(that.spanId) : that.spanId != null) return false;
        if (spanName != null ? !spanName.equals(that.spanName) : that.spanName != null) return false;
        if (traceId != null ? !traceId.equals(that.traceId) : that.traceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = traceId != null ? traceId.hashCode() : 0;
        result = 31 * result + (spanId != null ? spanId.hashCode() : 0);
        result = 31 * result + (spanName != null ? spanName.hashCode() : 0);
        result = 31 * result + (localAppKey != null ? localAppKey.hashCode() : 0);
        result = 31 * result + (localHost != null ? localHost.hashCode() : 0);
        result = 31 * result + localPort;
        result = 31 * result + (remoteAppKey != null ? remoteAppKey.hashCode() : 0);
        result = 31 * result + (remoteHost != null ? remoteHost.hashCode() : 0);
        result = 31 * result + remotePort;
        result = 31 * result + (int) (start ^ (start >>> 32));
        result = 31 * result + cost;
        result = 31 * result + type;
        result = 31 * result + status;
        result = 31 * result + count;
        result = 31 * result + debug;
        result = 31 * result + (extend != null ? extend.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SwiftSGModuleInvokeInfo{" +
                "traceId='" + traceId + '\'' +
                ", spanId='" + spanId + '\'' +
                ", spanName='" + spanName + '\'' +
                ", localAppKey='" + localAppKey + '\'' +
                ", localHost='" + localHost + '\'' +
                ", localPort=" + localPort +
                ", remoteAppKey='" + remoteAppKey + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                ", remotePort=" + remotePort +
                ", start=" + start +
                ", cost=" + cost +
                ", type=" + type +
                ", status=" + status +
                ", count=" + count +
                ", debug=" + debug +
                ", extend='" + extend + '\'' +
                '}';
    }
}
