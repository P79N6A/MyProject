package com.sankuai.octo.oswatch.model;

import java.util.List;

/**
 * Created by chenxi on 7/27/15.
 */
public class LogCollectorResult {
    String providerAppKey;
    List<ConsumerResult> consumer2QpsList;
    String spanName;

    public String getProviderAppKey() {
        return providerAppKey;
    }

    public void setProviderAppKey(String providerAppKey) {
        this.providerAppKey = providerAppKey;
    }

    public List<ConsumerResult> getConsumer2QpsList() {
        return consumer2QpsList;
    }

    public void setConsumer2QpsList(List<ConsumerResult> consumer2QpsList) {
        this.consumer2QpsList = consumer2QpsList;
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }
}

