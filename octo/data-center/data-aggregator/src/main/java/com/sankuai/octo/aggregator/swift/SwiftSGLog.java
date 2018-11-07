package com.sankuai.octo.aggregator.swift;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class SwiftSGLog {
    @ThriftField(1)
    public String appkey;
    @ThriftField(2)
    public long time;
    @ThriftField(3)
    public int level;
    @ThriftField(4)
    public String content;

    @ThriftConstructor
    public SwiftSGLog(String appkey, long time, int level, String content) {
        this.appkey = appkey;
        this.time = time;
        this.level = level;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwiftSGLog)) return false;

        SwiftSGLog that = (SwiftSGLog) o;

        if (level != that.level) return false;
        if (time != that.time) return false;
        if (appkey != null ? !appkey.equals(that.appkey) : that.appkey != null) return false;
        if (content != null ? !content.equals(that.content) : that.content != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = appkey != null ? appkey.hashCode() : 0;
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + level;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SwiftSGLog{" +
                "appkey='" + appkey + '\'' +
                ", time=" + time +
                ", level=" + level +
                ", content='" + content + '\'' +
                '}';
    }
}
