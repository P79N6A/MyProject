package com.sankuai.octo.msgp.domain.auth;

import java.util.HashMap;
import java.util.Map;

public class AccessConfig {
    private Integer ac;
    private Map<String, AccessConfig> conf = new HashMap<>();

    public AccessConfig() {
    }

    public AccessConfig(Integer ac) {
        this.ac = ac;
    }

    public Integer getAc() {
        return ac;
    }

    public void setAc(Integer ac) {
        this.ac = ac;
    }

    public Map<String, AccessConfig> getConf() {
        return conf;
    }

    public void setConf(Map<String, AccessConfig> conf) {
        this.conf = conf;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessConfig{");
        sb.append("ac=").append(ac);
        sb.append(", conf=").append(conf);
        sb.append('}');
        return sb.toString();
    }
}
