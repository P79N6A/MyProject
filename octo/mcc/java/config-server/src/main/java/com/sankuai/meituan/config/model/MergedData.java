package com.sankuai.meituan.config.model;

import java.util.Map;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-4-17
 */
public class MergedData {
    private String maxMatchPath;
    private Map<String, String> data;
    private Long version;

    public String getMaxMatchPath() {
        return maxMatchPath;
    }

    public void setMaxMatchPath(String maxMatchPath) {
        this.maxMatchPath = maxMatchPath;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "MergedData{" +
                "maxMatchPath='" + maxMatchPath + '\'' +
                ", data=" + data +
                ", version=" + version +
                '}';
    }
}
