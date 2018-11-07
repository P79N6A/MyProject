package com.sankuai.octo.oswatch.model;

import java.util.Map;

/**
 * Created by chenxi on 6/9/15.
 */

public class PerfQPSResult {
    String metric;
    PerfTags tags;
    Map<String, Double> dps;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public PerfTags getTags() {
        return tags;
    }

    public void setTags(PerfTags tags) {
        this.tags = tags;
    }

    public Map<String, Double> getDps() {
        return dps;
    }

    public void setDps(Map<String, Double> dps) {
        this.dps = dps;
    }
}
