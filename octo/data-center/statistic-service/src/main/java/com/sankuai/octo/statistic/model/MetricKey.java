package com.sankuai.octo.statistic.model;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

import java.io.Serializable;

@ThriftStruct
public class MetricKey implements Serializable{
    @ThriftField(1)
    public String appkey;
    @ThriftField(2)
    public String spanname;
    @ThriftField(3)
    public String localHost;
    @ThriftField(4)
    public String remoteAppKey;
    @ThriftField(5)
    public String remoteHost;
    @ThriftField(6)
    public StatSource source;
    @ThriftField(7)
    public PerfProtocolType perfProtocolType;
    @ThriftField(8)
    public String infraName;

    @ThriftConstructor
    public MetricKey(String appkey, String spanname, String localHost, String remoteAppKey, String remoteHost, StatSource source, PerfProtocolType perfProtocolType, String infraName) {
        this.appkey = appkey;
        this.spanname = spanname;
        this.localHost = localHost;
        this.remoteAppKey = remoteAppKey;
        this.remoteHost = remoteHost;
        this.source = source;
        this.perfProtocolType = perfProtocolType;
        this.infraName = infraName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricKey metricKey = (MetricKey) o;

        if (appkey != null ? !appkey.equals(metricKey.appkey) : metricKey.appkey != null) return false;
        if (infraName != null ? !infraName.equals(metricKey.infraName) : metricKey.infraName != null) return false;
        if (localHost != null ? !localHost.equals(metricKey.localHost) : metricKey.localHost != null) return false;
        if (perfProtocolType != metricKey.perfProtocolType) return false;
        if (remoteAppKey != null ? !remoteAppKey.equals(metricKey.remoteAppKey) : metricKey.remoteAppKey != null)
            return false;
        if (remoteHost != null ? !remoteHost.equals(metricKey.remoteHost) : metricKey.remoteHost != null) return false;
        if (source != metricKey.source) return false;
        if (spanname != null ? !spanname.equals(metricKey.spanname) : metricKey.spanname != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appkey != null ? appkey.hashCode() : 0;
        result = 31 * result + (spanname != null ? spanname.hashCode() : 0);
        result = 31 * result + (localHost != null ? localHost.hashCode() : 0);
        result = 31 * result + (remoteAppKey != null ? remoteAppKey.hashCode() : 0);
        result = 31 * result + (remoteHost != null ? remoteHost.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (perfProtocolType != null ? perfProtocolType.hashCode() : 0);
        result = 31 * result + (infraName != null ? infraName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MetricKey{" +
                "appkey='" + appkey + '\'' +
                ", spanname='" + spanname + '\'' +
                ", localHost='" + localHost + '\'' +
                ", remoteAppKey='" + remoteAppKey + '\'' +
                ", remoteHost='" + remoteHost + '\'' +
                ", source=" + source +
                ", perfProtocolType=" + perfProtocolType +
                ", infraName='" + infraName + '\'' +
                '}';
    }
}
