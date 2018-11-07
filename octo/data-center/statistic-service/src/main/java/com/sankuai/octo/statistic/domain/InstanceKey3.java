package com.sankuai.octo.statistic.domain;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.sankuai.octo.statistic.model.PerfProtocolType;
import com.sankuai.octo.statistic.model.StatEnv;
import com.sankuai.octo.statistic.model.StatSource;

@ThriftStruct
public class InstanceKey3 {
    @ThriftField(1)
    public String appKey;
    @ThriftField(2)
    public StatEnv env;
    @ThriftField(3)
    public StatSource source;
    @ThriftField(4)
    public PerfProtocolType perfProtocolType;

    @ThriftConstructor
    public InstanceKey3(String appKey,StatEnv env,StatSource source,PerfProtocolType perfProtocolType) {
            this.appKey = appKey;
            this.env = env;
            this.source = source;
            this.perfProtocolType = perfProtocolType;
    }

    public String getAppKey() {
        return appKey;
    }

    public StatEnv getEnv() {
        return env;
    }

    public StatSource getSource() {
        return source;
    }

    public PerfProtocolType getPerfProtocolType() {
        return perfProtocolType;
    }
}
