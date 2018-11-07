package com.sankuai.meituan.config.v1;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-4-29
 */
public class CacheConfigV1 implements Serializable {

    private static final long serialVersionUID = -5378463072990665531L;

    private String nodeName;
    private ConcurrentMap<String, String> config;
    private Long version;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public ConcurrentMap<String, String> getConfig() {
        return config;
    }

    public void setConfig(ConcurrentMap<String, String> config) {
        this.config = config;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "CacheConfig{" +
                "nodeName='" + nodeName + '\'' +
                ", config=" + config +
                ", version=" + version +
                '}';
    }
}
